package com.hayden.graphql.models.dataservice;

import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.reactivestreams.Publisher;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.*;
import org.springframework.graphql.support.DocumentSource;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class FederatedGraphQlClientBuilder extends AbstractGraphQlClientBuilder<FederatedGraphQlClientBuilder>
		implements GraphQlClient.Builder<FederatedGraphQlClientBuilder>, AutoCloseable, IFederatedGraphQlClientBuilder {

	protected static final boolean jackson2Present = ClassUtils.isPresent(
			"com.fasterxml.jackson.databind.ObjectMapper", AbstractGraphQlClientBuilder.class.getClassLoader());


	private final List<GraphQlClientInterceptor> interceptors = new ArrayList<>();


	@Nullable
	private Encoder<?> jsonEncoder;

	@Nullable
	private Decoder<?> jsonDecoder;



	@Override
	public GraphQlClient build() {
		throw new NotImplementedException("Failed...");
	}

	public FederatedGraphQlClient buildFederatedClient(FederatedGraphQlTransport transport) {
		DefaultFederatedGraphQlClient graphQlClient = buildGraphQlClient(transport);
		return new FederatedGraphQlClient(graphQlClient, this);
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

		return new DefaultFederatedGraphQlClient(
				createExecuteChain(transport),
				createExecuteSubscriptionChain(transport)
		);
	}

	private GraphQlFederatedInterceptor.Chain createExecuteChain(FederatedGraphQlTransport transport) {
		GraphQlFederatedInterceptor.FederatedChain chain = request -> {
			return Mono.justOrEmpty(transport.retrieve(request))
					.flatMap(transportFound -> transportFound.execute(request)
							.map(response ->new DefaultClientGraphQlResponse(request, response, getEncoder(), getDecoder()))
					);
		};

		return this.interceptors.stream()
				.reduce(GraphQlClientInterceptor::andThen)
				.map(interceptor -> (GraphQlClientInterceptor.Chain) (request) -> interceptor.intercept(request, chain))
				.orElse(chain);
	}

	private GraphQlFederatedInterceptor.SubscriptionChain createExecuteSubscriptionChain(GraphQlTransport transport) {

		GraphQlFederatedInterceptor.FederatedSubscriptionChain chain = request -> transport.executeSubscription(request)
				.map(response -> new DefaultClientGraphQlResponse(request, response, getEncoder(), getDecoder()));

		return this.interceptors.stream()
				.reduce(GraphQlClientInterceptor::andThen)
				.map(interceptor -> (GraphQlClientInterceptor.SubscriptionChain) (request) -> interceptor.interceptSubscription(request, chain))
				.orElse(chain);
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
		// proxied to add back to connection pool
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
	public static class FederatedGraphQlClient implements DataServiceRequestExecutor, AutoCloseable, GraphQlClient{

		@Delegate
		private final DefaultFederatedGraphQlClient delegate;
		private final FederatedGraphQlClientBuilder parent;

		FederatedGraphQlClient(
				DefaultFederatedGraphQlClient delegate,
				FederatedGraphQlClientBuilder parent
		) {
			this.delegate = delegate;
			this.parent = parent;
		}

		@Override
		public Publisher<FederatedGraphQlResponse> request(FederatedRequestData federatedRequestData) {
			return this.delegate.federatedDocuments(federatedRequestData)
					.executeSubscription()
					.doOnEach(item -> logExecError(item.getClass(), item))
					.map(c -> new FederatedGraphQlResponseItem(c.getData(), c))
					.collectList()
					.map(f -> new FederatedGraphQlResponse(f.toArray(FederatedGraphQlResponseItem[]::new)));
		}

		@Override
		public void close() {
			// proxied to add back to connection pool
			this.parent.close();
		}
	}

	protected static class DefaultJackson2Codecs {

		static Encoder<?> encoder() {
			return new Jackson2JsonEncoder();
		}

		static Decoder<?> decoder() {
			return new Jackson2JsonDecoder();
		}

	}

}
