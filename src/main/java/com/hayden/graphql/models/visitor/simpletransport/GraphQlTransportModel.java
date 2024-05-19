package com.hayden.graphql.models.visitor.simpletransport;

import com.hayden.graphql.federated.transport.http.HttpGraphQlTransportBuilder;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceFetcherItemId;
import com.hayden.graphql.models.visitor.model.VisitorModel;
import lombok.extern.slf4j.Slf4j;

/**
 * TODO: create a request for delete of schema from TypeRegistry.
 * @param serviceId the serviceId of this graph-ql service. This will be the same for deploying multiple services of
 *                  the same type.
 * @param source the GraphQlSource schema provided.
 * @param fetcherSource the source code for the params fetchers so this service id can be federated.
 */
@Slf4j
public record GraphQlTransportModel(
        FederatedGraphQlServiceFetcherItemId serviceId,
        HttpGraphQlTransportBuilder transportBuilder,
        FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceInstanceId id,
        boolean invalidateCurrent
) implements VisitorModel {
}
