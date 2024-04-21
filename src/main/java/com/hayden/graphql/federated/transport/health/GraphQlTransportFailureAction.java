package com.hayden.graphql.federated.transport.health;

public interface GraphQlTransportFailureAction {
    void failureEvent();

    boolean matches(Throwable throwable);
}
