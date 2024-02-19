package com.hayden.graphql.federated.transport;

import com.hayden.graphql.models.dataservice.AggregateRemoteDataFederation;
import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.graphql.client.ClientGraphQlResponse;

@RequiredArgsConstructor
public class RemoteAggregateFederatedItemGraphQlTransport implements FederatedItemGraphQlTransport<ClientFederatedRequestItem> {

    private final AggregateRemoteDataFederation remoteDataFetcher;

    @Override
    public Publisher<ClientGraphQlResponse> next(ClientFederatedRequestItem request) {
        return remoteDataFetcher.get(request.getRequestData(), request.getClient());
    }

}
