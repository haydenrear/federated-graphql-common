package com.hayden.graphql.federated.client;


import com.hayden.graphql.federated.transport.federated_transport.FederatedGraphQlTransportResult;

public interface IFederatedGraphQlClientBuilder extends AutoCloseable {
    FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient buildFederatedClient(FederatedGraphQlTransportResult requestData);
}
