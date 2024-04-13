package com.hayden.graphql.federated.transport;

import com.hayden.graphql.models.dataservice.AggregateRemoteDataFederation;
import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.client.ClientGraphQlResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RemoteAggregateFederatedItemGraphQlTransport implements FederatedItemGraphQlTransport<ClientFederatedRequestItem> {

    private final AggregateRemoteDataFederation remoteDataFetcher;

    @Override
    public Publisher<ClientGraphQlResponse> next(ClientFederatedRequestItem request) {
        return remoteDataFetcher.get(request.getRequestData(), request.getClient());
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
