package com.hayden.graphql.models.dataservice;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.graphql.client.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Default, final {@link GraphQlClient} implementation for use with any transport.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
public class DefaultFederatedGraphQlClient implements GraphQlClient {

    private final GraphQlFederatedInterceptor.Chain executeChain;
    private final GraphQlFederatedInterceptor.SubscriptionChain executeSubscriptionChain;


    DefaultFederatedGraphQlClient(
            GraphQlFederatedInterceptor.Chain executeChain,
            GraphQlFederatedInterceptor.SubscriptionChain executeSubscriptionChain) {
        Assert.notNull(executeChain, "GraphQlClientInterceptor.Chain is required");
        Assert.notNull(executeSubscriptionChain, "GraphQlClientInterceptor.SubscriptionChain is required");

        this.executeChain = executeChain;
        this.executeSubscriptionChain = executeSubscriptionChain;
    }

    public <T> RequestSpec federatedDocuments(DataServiceRequestExecutor.FederatedRequestData requestData) {
        return new FederatedRequestSpec(requestData);
    }


    @Override
    public RequestSpec document(String document) {
        throw new NotImplementedException("Only document.");
    }

    @Override
    public RequestSpec documentName(String name) {
        throw new NotImplementedException("not implemented");
    }

    /**
     * The default client is unaware of transport details, and cannot implement
     * mutate directly. It should be wrapped from transport aware extensions via
     * {@link AbstractDelegatingGraphQlClient} that also implement mutate.
     */
    @Override
    public Builder<?> mutate() {
        throw new UnsupportedOperationException();
    }


    /**
     * Default {@link RequestSpec} implementation.
     */
    @RequiredArgsConstructor
    private final class FederatedRequestSpec implements RequestSpec {

        private final DataServiceRequestExecutor.FederatedRequestData documentMono;


        @Override
        public RequestSpec operationName(String operationName) {
            throw new NotImplementedException("not implemented");
        }

        @Override
        public RequestSpec variable(String name, Object value) {
            throw new NotImplementedException("not implemented");
        }

        @Override
        public RequestSpec variables(Map<String, Object> variables) {
            throw new NotImplementedException("not implemented");
        }

        @Override
        public RequestSpec extension(String name, Object value) {
            throw new NotImplementedException("not implemented");
        }

        @Override
        public RequestSpec extensions(Map<String, Object> extensions) {
            throw new NotImplementedException("not implemented");
        }

        @Override
        public RequestSpec attribute(String name, Object value) {
            throw new NotImplementedException("not implemented");
        }

        @Override
        public RequestSpec attributes(Consumer<Map<String, Object>> attributesConsumer) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RetrieveSpec retrieve(String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public RetrieveSubscriptionSpec retrieveSubscription(String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Mono<ClientGraphQlResponse> execute() {
            throw new UnsupportedOperationException();
        }

        public Flux<ClientGraphQlResponse> executeSubscription() {
            return initRequest().flatMapMany(request -> executeSubscriptionChain.next(request)
                    .onErrorResume(
                            ex -> !(ex instanceof GraphQlClientException),
                            ex -> Mono.error(new GraphQlTransportException(ex, request))));
        }

        private Mono<FederatedGraphQlRequest> initRequest() {
            return Flux.fromArray(this.documentMono.data())
                    .map(document -> Pair.of(document.federatedService(), new DefaultClientGraphQlRequest(
                            (String) document.requestBody(), document.operationName(),
                            document.variables(), document.extensions(), document.attributes()
                    )))
                    .map(p -> Map.entry(p.getKey(), new FederatedGraphQlRequest.FederatedClientGraphQlRequestItem(p.getKey(), p.getRight())))
                    .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                    .map(FederatedGraphQlRequest::new);
        }

    }


    private static class RetrieveSpecSupport {

        private final String path;

        protected RetrieveSpecSupport(String path) {
            this.path = path;
        }

        /**
         * Return the field or {@code null}, but only if the response is valid and
         * there are no field errors. Raise {@link FieldAccessException} otherwise.
         * @throws FieldAccessException in case of an invalid response or any
         * field error at, above or below the field path
         */
        @Nullable
        protected ClientResponseField getValidField(ClientGraphQlResponse response) {
            ClientResponseField field = response.field(this.path);
            if (!response.isValid() || !field.getErrors().isEmpty()) {
                throw new UnsupportedOperationException();
            }
            return (field.getValue() != null ? field : null);
        }

    }


    private static class FederatedRetrieveSpec extends RetrieveSpecSupport implements RetrieveSpec {

        private final Mono<ClientGraphQlResponse> responseMono;

        FederatedRetrieveSpec(Mono<ClientGraphQlResponse> responseMono, String path) {
            super(path);
            this.responseMono = responseMono;
        }

        @Override
        public <D> Mono<D> toEntity(Class<D> entityType) {
            return this.responseMono.mapNotNull(this::getValidField).mapNotNull(field -> field.toEntity(entityType));
        }

        @Override
        public <D> Mono<D> toEntity(ParameterizedTypeReference<D> entityType) {
            return this.responseMono.mapNotNull(this::getValidField).mapNotNull(field -> field.toEntity(entityType));
        }

        @Override
        public <D> Mono<List<D>> toEntityList(Class<D> elementType) {
            return this.responseMono.map(response -> {
                ClientResponseField field = getValidField(response);
                return (field != null ? field.toEntityList(elementType) : Collections.emptyList());
            });
        }

        @Override
        public <D> Mono<List<D>> toEntityList(ParameterizedTypeReference<D> elementType) {
            return this.responseMono.map(response -> {
                ClientResponseField field = getValidField(response);
                return (field != null ? field.toEntityList(elementType) : Collections.emptyList());
            });
        }

    }


    private static class DefaultRetrieveSubscriptionSpec extends RetrieveSpecSupport implements RetrieveSubscriptionSpec {

        private final Flux<ClientGraphQlResponse> responseFlux;

        DefaultRetrieveSubscriptionSpec(Flux<ClientGraphQlResponse> responseFlux, String path) {
            super(path);
            this.responseFlux = responseFlux;
        }

        @Override
        public <D> Flux<D> toEntity(Class<D> entityType) {
            return this.responseFlux.mapNotNull(this::getValidField).mapNotNull(field -> field.toEntity(entityType));
        }

        @Override
        public <D> Flux<D> toEntity(ParameterizedTypeReference<D> entityType) {
            return this.responseFlux.mapNotNull(this::getValidField).mapNotNull(field -> field.toEntity(entityType));
        }

        @Override
        public <D> Flux<List<D>> toEntityList(Class<D> elementType) {
            return this.responseFlux.map(response -> {
                ClientResponseField field = getValidField(response);
                return (field != null ? field.toEntityList(elementType) : Collections.emptyList());
            });
        }

        @Override
        public <D> Flux<List<D>> toEntityList(ParameterizedTypeReference<D> elementType) {
            return this.responseFlux.map(response -> {
                ClientResponseField field = getValidField(response);
                return (field != null ? field.toEntityList(elementType) : Collections.emptyList());
            });
        }

    }

}
