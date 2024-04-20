package com.hayden.graphql.federated.client;

import com.hayden.graphql.federated.execution.DataServiceRequestExecutor;
import com.hayden.graphql.federated.transport.FederatedGraphQlTransportResult;
import com.hayden.graphql.federated.interceptor.GraphQlFederatedInterceptor;
import com.hayden.graphql.federated.transport.FederatedGraphQlTransport;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import com.hayden.graphql.models.federated.response.FederatedClientGraphQlResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.*;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Signal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class FederatedGraphQlClientBuilderHolder extends AbstractGraphQlClientBuilder<FederatedGraphQlClientBuilderHolder>
		implements GraphQlClient.Builder<FederatedGraphQlClientBuilderHolder>, AutoCloseable, IFederatedGraphQlClientBuilder {

	protected static final boolean jackson2Present = ClassUtils.isPresent(
			"com.fasterxml.jackson.databind.ObjectMapper", AbstractGraphQlClientBuilder.class.getClassLoader());


	private final List<GraphQlClientInterceptor> interceptors = List.of(new GraphQlFederatedInterceptor());

	private FederatedGraphQlClient clientBuilt;

	/**
	 * Connection pool proxy ref for close to add back to connection pool. Sort of like self-decorator - pass myself to someone
	 * else, that someone else passes me myself decorated and now I use that ref to call.
	 */
	@Setter
	private IFederatedGraphQlClientBuilder proxyRef;


	@Nullable
	private Encoder<?> jsonEncoder;

	@Nullable
	private Decoder<?> jsonDecoder;

	@Override
	public GraphQlClient build() {
		throw new NotImplementedException("Use buildFederatedClient to build %s.".formatted(this.getClass().getSimpleName()));
	}

	public FederatedGraphQlClient buildFederatedClient(FederatedGraphQlTransportResult transport) {
		return Optional.ofNullable(this.clientBuilt)
				// rebuild client required to add new data fetchers from DGS, as DataFetchers are how Federation is
				// implemented.
				.filter(buildClientExists -> !transport.doReload())
				.orElseGet(() -> {
					DefaultFederatedGraphQlClient graphQlClient = buildGraphQlClient(transport.transport());
					return new FederatedGraphQlClient(graphQlClient, Optional.ofNullable(proxyRef).orElse(this));
				});
	}

	/**
	 * Build the default transport-agnostic client that subclasses can then wrap
	 * with {@link AbstractDelegatingGraphQlClient}.
	 */
	protected DefaultFederatedGraphQlClient buildGraphQlClient(FederatedGraphQlTransport transport) {

		if (jackson2Present) {
			this.jsonEncoder = (this.jsonEncoder == null ? DefaultJackson2Codecs.encoder() : this.jsonEncoder);
			this.jsonDecoder = (this.jsonDecoder == null ? DefaultJackson2Codecs.decoder() : this.jsonDecoder);
		}

		return new DefaultFederatedGraphQlClient(createExecuteSubscriptionChain(transport));
	}

	private GraphQlFederatedInterceptor.FederatedSubscriptionChain createExecuteSubscriptionChain(FederatedGraphQlTransport transport) {

		GraphQlFederatedInterceptor.FederatedSubscriptionChain chain = request -> transport.executeSubscription(request)
				.map(g -> mapToResponse(request, g))
                .map(response -> new FederatedClientGraphQlResponse(request, response, getEncoder(), getDecoder()));

		return this.interceptors.stream()
				.reduce(GraphQlClientInterceptor::andThen)
				.map(interceptor -> toSubscriptionChain(interceptor, chain))
				.orElse(chain);
	}

	@NotNull
	private static GraphQlFederatedInterceptor.FederatedSubscriptionChain toSubscriptionChain(
			GraphQlClientInterceptor interceptor,
			GraphQlFederatedInterceptor.FederatedSubscriptionChain chain
	) {
		return request -> interceptor.interceptSubscription(request, chain);
	}

	@NotNull
	private FederatedClientGraphQlResponse mapToResponse(ClientGraphQlRequest request, GraphQlResponse g) {
		if (g instanceof FederatedClientGraphQlResponse r) {
			return r;
		} else {
			return new FederatedClientGraphQlResponse(request, g, this.jsonEncoder, this.jsonDecoder);
		}
	}


	private static <ResponseT> void logExecError(Class<ResponseT> clzz, Signal<ClientGraphQlResponse> item) {
		if (item.hasError()){
			log.error(
					"Errors when requesting: {}, {}", clzz.getSimpleName(),
					Optional.ofNullable(item.get())
							.stream()
							.flatMap(c -> c.getErrors().stream())
							.map(ResponseError::getMessage)
							.collect(Collectors.joining(",")));
		}
	}

	@Override
	public void close() {
		log.info("Closing...");
	}

	private Encoder<?> getEncoder() {
		Assert.notNull(this.jsonEncoder, "jsonEncoder has not been set");
		return this.jsonEncoder;
	}

	private Decoder<?> getDecoder() {
		Assert.notNull(this.jsonDecoder, "jsonDecoder has not been set");
		return this.jsonDecoder;
	}


	/**
	 * Default {@link HttpGraphQlClient} implementation.
	 */
	public static class FederatedGraphQlClient implements DataServiceRequestExecutor, AutoCloseable {

		@Delegate
		private final DefaultFederatedGraphQlClient delegate;
		private final IFederatedGraphQlClientBuilder parent;

		FederatedGraphQlClient(
				DefaultFederatedGraphQlClient delegate,
				IFederatedGraphQlClientBuilder parent
		) {
			this.delegate = delegate;
			this.parent = parent;
		}

		public record FederatedGraphQlRequestArgs(@NotNull FederatedGraphQlClient federatedGraphQlClient) {}

		@Override
		public Publisher<FederatedGraphQlResponse> request(FederatedRequestData federatedRequestData) {
			return this.delegate.federatedDocuments(federatedRequestData, new FederatedGraphQlRequestArgs(this))
					.execute()
					.doOnEach(item -> logExecError(item.getClass(), item))
					.map(c -> new FederatedGraphQlResponseItem(c.getData(), c))
					.collectList()
					.map(f -> new FederatedGraphQlResponse(f.toArray(FederatedGraphQlResponseItem[]::new)));
		}

		public Flux<ClientGraphQlResponse> requestItem(FederatedRequestData federatedRequestData) {
			return this.delegate.federatedDocuments(federatedRequestData, new FederatedGraphQlRequestArgs(this))
					.execute();
		}

		@SneakyThrows
        @Override
		public void close() {
			// proxied to add back to connection pool
			this.parent.close();
		}
	}

	public static class DefaultJackson2Codecs {

		public static Encoder<?> encoder() {
			return new Jackson2JsonEncoder();
		}

		public static Decoder<?> decoder() {
			return new Jackson2JsonDecoder();
		}

	}

}
