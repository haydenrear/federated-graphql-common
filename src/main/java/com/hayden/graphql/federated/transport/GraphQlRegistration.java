package com.hayden.graphql.federated.transport;

import com.hayden.graphql.models.federated.request.FederatedGraphQlRequest;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import com.hayden.graphql.models.visitor.DataTemplate;
import org.springframework.graphql.client.GraphQlTransport;

public interface GraphQlRegistration {
    FederatedGraphQlServiceItemId id();

    record GraphQlTransportFederatedGraphQlRegistration(
            GraphQlTransport graphQlTransport,
            FederatedGraphQlServiceItemId id,
            DataTemplate dataTemplate) implements GraphQlRegistration {}

    record FederatedItemFederatedGraphQlTransportRegistration(FederatedItemGraphQlTransport<FederatedGraphQlRequest.FederatedClientGraphQlRequestItem> graphQlTransport,
                                                              FederatedGraphQlServiceItemId id) implements GraphQlRegistration {
                                                               }
}
