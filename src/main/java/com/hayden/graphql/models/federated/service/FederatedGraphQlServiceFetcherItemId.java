package com.hayden.graphql.models.federated.service;

import org.springframework.util.MimeType;

/**
 * Each query needs a unique registration, and each service needs a transport registration.
 * @param id
 * @param serviceId
 * @param host
 */
public record FederatedGraphQlServiceFetcherItemId(FederatedGraphQlServiceFetcherId id,
                                                   FederatedGraphQlServiceInstanceId serviceInstanceId) {
    public record FederatedGraphQlServiceFetcherId(MimeType mimeType, String uniqueFetchId, FederatedGraphQlServiceId serviceId) { }
    public record FederatedGraphQlServiceId(String serviceId) {}
    public record FederatedGraphQlHost(String host) {}
    public record FederatedGraphQlServiceInstanceId(FederatedGraphQlServiceId serviceId, FederatedGraphQlHost host) {}
}
