package com.hayden.graphql.federated.client;

import java.util.Map;

import com.hayden.graphql.federated.interceptor.GraphQlFederatedInterceptor;
import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import com.hayden.graphql.models.federated.request.FederatedGraphQlRequest;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.graphql.client.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.util.Assert;

/**
 * Default, final {@link GraphQlClient} implementation for use with any transport.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
public class DefaultFederatedGraphQlClient {

    private final GraphQlFederatedInterceptor.FederatedSubscriptionChain executeSubscriptionChain;


    DefaultFederatedGraphQlClient(GraphQlFederatedInterceptor.FederatedSubscriptionChain executeSubscriptionChain) {
        Assert.notNull(executeSubscriptionChain, "GraphQlClientInterceptor.SubscriptionChain is required");
        this.executeSubscriptionChain = executeSubscriptionChain;
    }

    public FederatedRequestSpec federatedDocuments(FederatedRequestData requestData,
                                                   FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs federatedGraphQlClient) {
        return new FederatedRequestSpec(requestData, federatedGraphQlClient);
    }


    @RequiredArgsConstructor
    public final class FederatedRequestSpec  {

        private final FederatedRequestData requestData;
        private final FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs client;


        public Flux<ClientGraphQlResponse> execute() {
            return initRequest()
                    .flatMapMany(request -> executeSubscriptionChain.next(request)
                    .onErrorResume(
                            ex -> !(ex instanceof GraphQlClientException),
                            ex -> Mono.error(new GraphQlTransportException(ex, request))));
        }

        private Mono<FederatedGraphQlRequest> initRequest() {
            return Flux.fromArray(this.requestData.data())
                    .map(document -> Pair.of(document.federatedService(), new ClientFederatedRequestItem(
                            document.requestBody(),
                            document.operationName(),
                            document.variables(),
                            document.extensions(),
                            document.attributes(),
                            requestData, client
                    )))
                    .map(p -> Map.entry(p.getKey(), new FederatedGraphQlRequest.FederatedClientGraphQlRequestItem(p.getKey(), p.getRight())))
                    .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                    .map(FederatedGraphQlRequest::new);
        }

    }

}
