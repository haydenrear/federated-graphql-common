package com.hayden.graphql.models.dataservice;

import lombok.experimental.Delegate;
import org.springframework.graphql.client.ClientGraphQlRequest;

import java.util.Map;
import java.util.Optional;

public record FederatedGraphQlRequest(Map<FederatedGraphQlService, FederatedClientGraphQlRequestItem> delegators) implements ClientGraphQlRequest {

    public record FederatedClientGraphQlRequestItem(FederatedGraphQlService service, @Delegate ClientGraphQlRequest clientGraphQlRequest) implements ClientGraphQlRequest {}

    public Optional<ClientGraphQlRequest> service(FederatedGraphQlService service) {
        return Optional.ofNullable(delegators.get(service)).map(FederatedClientGraphQlRequestItem::clientGraphQlRequest);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public String getDocument() {
        return null;
    }

    @Override
    public String getOperationName() {
        return null;
    }

    @Override
    public Map<String, Object> getVariables() {
        return null;
    }

    @Override
    public Map<String, Object> getExtensions() {
        return null;
    }

    @Override
    public Map<String, Object> toMap() {
        return null;
    }
}
