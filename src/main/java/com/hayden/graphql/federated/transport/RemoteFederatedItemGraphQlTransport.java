package com.hayden.graphql.federated.transport;

import com.hayden.graphql.models.dataservice.RemoteDataFederation;
import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.graphql.client.ClientGraphQlResponse;

@RequiredArgsConstructor
public class RemoteFederatedItemGraphQlTransport implements FederatedItemGraphQlTransport<ClientFederatedRequestItem> {

    private final RemoteDataFederation remoteDataFetcher;

    @Override
    public Publisher<ClientGraphQlResponse> next(ClientFederatedRequestItem clientFederatedRequestItem) {
        return remoteDataFetcher.get(clientFederatedRequestItem.getRequestData(), clientFederatedRequestItem.getClient());
    }

}
