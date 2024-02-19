package com.hayden.graphql.federated.transport;

import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import org.reactivestreams.Publisher;
import org.springframework.graphql.client.ClientGraphQlResponse;

/**
 * The standard query request graphQL transport.
 */
public class QueryFederatedGraphQlTransportItem implements FederatedItemGraphQlTransport {
    @Override
    public Publisher<ClientGraphQlResponse> next(ClientFederatedRequestItem request) {
        return request.getClient().federatedGraphQlClient()
                .requestItem(request.getRequestData());
    }
}
