package com.hayden.graphql.models.federated.service;

import org.springframework.util.MimeType;

/**
 * Each query needs a unique registration, and each service needs a transport registration.
 * @param id
 * @param serviceId
 * @param host
 */
public record FederatedGraphQlServiceFetcherItemId(
        FederatedGraphQlServiceFetcherId id, String serviceId, String host) {
    public record FederatedGraphQlServiceFetcherId(MimeType mimeType, String uniqueFetchId, String serviceId) {
    }
}
