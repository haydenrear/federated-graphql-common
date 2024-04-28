package com.hayden.graphql.models.federated.request;

import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import lombok.experimental.Delegate;
import org.springframework.graphql.client.ClientGraphQlRequest;

import java.util.Map;
import java.util.Optional;

/**
 * Delegator request. 2 Ways:
 * 1. Send a normal GraphQlRequest, map to DataFetchers provided by the services, authenticated with hash and pre-compiled or compiled on the fly
 * 1.1 For each remote data fetcher - produce FederatedGraphQlRequest
 *    1.2 For all sub-requests in each FederatedGraphQlRequest (below delegators)
 *          1.2.1 - get ClientGraphQlRequest
 *          1.2.2 - call HttpGraphQlTransport (for each other service)
 *          1.2.3 - produce response as subscription
 * 2. Start at receiving FederatedGraphQlRequest or something derivated from directly from the user (skip GraphQl parsing), start from 1.2
 *
 * **Note**: Federated GraphQl remote data fetchers have the option to reference many other service IDS (below key in map).
 * TODO: when compiling the DataFetcher, the dependencies need to be asserted. A dependency graph therefore needs to be referenced and maintained.
 *  When a request fails, the service is removed from memory. Should the compiled files be deleted? What is the limit on files to be added? When
 *  no GraphQl service instance is referenced for a particular MimeType, a particular schema, when the query fires it will fail and be removed, but
 *  all dependencies that are not up or were removed should be returned in the error message.
 * @param delegators
 */
public record FederatedGraphQlRequest(Map<FederatedGraphQlServiceItemId.FederatedGraphQlServiceId, FederatedClientGraphQlRequestItem> delegators)
        implements ClientGraphQlRequest {

    public record FederatedClientGraphQlRequestItem(FederatedGraphQlServiceItemId.FederatedGraphQlServiceId service,
                                                    @Delegate ClientGraphQlRequest clientGraphQlRequest)
            implements ClientGraphQlRequest {}

    public Optional<ClientGraphQlRequest> service(FederatedGraphQlServiceItemId.FederatedGraphQlServiceId service) {
        return Optional.ofNullable(delegators.get(service))
                .map(FederatedClientGraphQlRequestItem::clientGraphQlRequest);
    }

    @Override
    public Map<String, Object> getAttributes() {
        throw new UnsupportedOperationException(logNotSupported());
    }

    private String logNotSupported() {
        return "Called method on %s. This is a delegator class.".formatted(this.getClass().getSimpleName());
    }

    @Override
    public String getDocument() {
        throw new UnsupportedOperationException(logNotSupported());
    }

    @Override
    public String getOperationName() {
        throw new UnsupportedOperationException(logNotSupported());
    }

    @Override
    public Map<String, Object> getVariables() {
        throw new UnsupportedOperationException(logNotSupported());
    }

    @Override
    public Map<String, Object> getExtensions() {
        throw new UnsupportedOperationException(logNotSupported());
    }

    @Override
    public Map<String, Object> toMap() {
        throw new UnsupportedOperationException(logNotSupported());
    }
}
