package com.hayden.graphql.federated.client;


import com.hayden.graphql.federated.transport.FederatedGraphQlTransportResult;

public interface IFederatedGraphQlClientBuilder {
    FederatedGraphQlClientBuilder.FederatedGraphQlClient buildFederatedClient(FederatedGraphQlTransportResult requestData);
}
