package com.hayden.graphql.federated.transport;

import com.hayden.graphql.models.dataservice.RemoteDataFederation;
import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.client.ClientGraphQlResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class RemoteFederatedItemGraphQlTransport implements FederatedItemGraphQlTransport<ClientFederatedRequestItem> {

    private final RemoteDataFederation remoteDataFetcher;

    @Override
    public Publisher<ClientGraphQlResponse> next(ClientFederatedRequestItem clientFederatedRequestItem) {
        return remoteDataFetcher.get(clientFederatedRequestItem.getRequestData(), clientFederatedRequestItem.getClient());
    }

    @Override
    public @NotNull Mono<GraphQlResponse> execute(@NotNull GraphQlRequest request) {
        if (request instanceof ClientFederatedRequestItem federatedRequestItem)
            return Mono.from(next(federatedRequestItem));

        return Mono.empty();
    }

    @Override
    public @NotNull Flux<GraphQlResponse> executeSubscription(@NotNull GraphQlRequest request) {
        if (request instanceof ClientFederatedRequestItem federatedRequestItem)
            return Flux.from(next(federatedRequestItem));

        return Flux.empty();
    }
}
