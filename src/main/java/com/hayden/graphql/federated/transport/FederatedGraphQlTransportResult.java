package com.hayden.graphql.federated.transport;

import com.hayden.graphql.federated.transport.FederatedGraphQlTransport;

public record FederatedGraphQlTransportResult(
        boolean doReload,
        FederatedGraphQlTransport transport) {
}
