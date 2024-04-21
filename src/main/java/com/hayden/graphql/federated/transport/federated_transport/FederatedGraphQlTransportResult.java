package com.hayden.graphql.federated.transport.federated_transport;

public record FederatedGraphQlTransportResult(
        boolean doReload,
        FederatedGraphQlTransport transport) {
}
