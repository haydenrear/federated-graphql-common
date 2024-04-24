package com.hayden.graphql.models.federated.service;

import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

public record FederatedGraphQlServiceItemId(FederatedGraphQlServiceId id, String serviceId, String host) {
    public record FederatedGraphQlServiceId(MimeType mimeType, String uniqueFetchId, String serviceId) {}
}
