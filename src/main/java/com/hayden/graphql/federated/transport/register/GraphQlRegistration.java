package com.hayden.graphql.federated.transport.register;

import com.hayden.graphql.federated.transport.federated_transport.FederatedItemGraphQlTransport;
import com.hayden.graphql.models.federated.request.FederatedGraphQlRequest;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceFetcherItemId;
import com.hayden.graphql.models.visitor.model.DataTemplate;
import org.springframework.graphql.client.GraphQlTransport;

public interface GraphQlRegistration {

    FederatedGraphQlServiceFetcherItemId id();

    /**
     * Access single graphql service, through HttpGraphQlTransport, for instance.
     * @param graphQlTransport
     * @param id
     * @param dataTemplate
     */
    record GraphQlTransportFederatedGraphQlRegistration(
            GraphQlTransport graphQlTransport,
            FederatedGraphQlServiceFetcherItemId id,
            DataTemplate dataTemplate) implements GraphQlRegistration {}

    /**
     * Access the DGS context, to get the data fetcher that will get the above registration eventually.
     * @param graphQlTransport
     * @param id
     */
    record FederatedItemFederatedGraphQlTransportRegistration(
            FederatedItemGraphQlTransport<FederatedGraphQlRequest.FederatedClientGraphQlRequestItem> graphQlTransport,
            FederatedGraphQlServiceFetcherItemId id) implements GraphQlRegistration { }
}
