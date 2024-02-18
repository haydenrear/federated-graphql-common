package com.hayden.graphql.models.dataservice;


public interface IFederatedGraphQlClientBuilder {
    FederatedGraphQlClientBuilder.FederatedGraphQlClient buildFederatedClient(FederatedGraphQlTransport requestData);
}
