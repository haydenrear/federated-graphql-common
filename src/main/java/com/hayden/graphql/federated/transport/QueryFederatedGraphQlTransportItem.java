package com.hayden.graphql.federated.transport;

import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import org.reactivestreams.Publisher;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.client.ClientGraphQlResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The standard query request graphQL transport.
 */
public class QueryFederatedGraphQlTransportItem implements FederatedItemGraphQlTransport<ClientFederatedRequestItem> {
    @Override
    public Publisher<ClientGraphQlResponse> next(ClientFederatedRequestItem request) {
        return request.getClient().federatedGraphQlClient()
                .requestItem(request.getRequestData());
    }

    @Override
    public Mono<GraphQlResponse> execute(GraphQlRequest request) {
        if (request instanceof ClientFederatedRequestItem federatedRequestItem)
            return Mono.from(next(federatedRequestItem));

        return Mono.empty();
    }

    @Override
    public Flux<GraphQlResponse> executeSubscription(GraphQlRequest request) {
        if (request instanceof ClientFederatedRequestItem federatedRequestItem)
            return Flux.from(next(federatedRequestItem));

        return Flux.empty();
    }
}
