package com.hayden.graphql.models.federated.request;

import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import lombok.experimental.Delegate;
import org.springframework.graphql.client.ClientGraphQlRequest;

import java.util.Map;
import java.util.Optional;

public record FederatedGraphQlRequest(Map<FederatedGraphQlServiceItemId, FederatedClientGraphQlRequestItem> delegators)
        implements ClientGraphQlRequest {

    public record FederatedClientGraphQlRequestItem(
            FederatedGraphQlServiceItemId service, @Delegate ClientGraphQlRequest clientGraphQlRequest)
            implements ClientGraphQlRequest {}

    public Optional<ClientGraphQlRequest> service(FederatedGraphQlServiceItemId service) {
        return Optional.ofNullable(delegators.get(service))
                .map(FederatedClientGraphQlRequestItem::clientGraphQlRequest);
    }

    @Override
    public Map<String, Object> getAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDocument() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getOperationName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getVariables() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getExtensions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> toMap() {
        throw new UnsupportedOperationException();
    }
}
