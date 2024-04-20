package com.hayden.graphql.federated.transport;

import com.hayden.graphql.models.federated.request.FederatedGraphQlRequest;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import com.hayden.graphql.models.visitor.DataTemplate;
import org.springframework.graphql.client.GraphQlTransport;

public interface GraphQlRegistration {

    FederatedGraphQlServiceItemId id();

    /**
     * Access single graphql service, through HttpGraphQlTransport, for instance.
     * @param graphQlTransport
     * @param id
     * @param dataTemplate
     */
    record GraphQlTransportFederatedGraphQlRegistration(
            GraphQlTransport graphQlTransport,
            FederatedGraphQlServiceItemId id,
            DataTemplate dataTemplate) implements GraphQlRegistration {}

    /**
     * Access the DGS context, to get the data fetcher that will get the above registration eventually.
     * @param graphQlTransport
     * @param id
     */
    record FederatedItemFederatedGraphQlTransportRegistration(
            FederatedItemGraphQlTransport<FederatedGraphQlRequest.FederatedClientGraphQlRequestItem> graphQlTransport,
            FederatedGraphQlServiceItemId id) implements GraphQlRegistration { }
}
