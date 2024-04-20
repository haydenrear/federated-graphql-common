package com.hayden.graphql.models.visitor.datafed;

import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import com.hayden.graphql.models.visitor.schema.GraphQlFederatedSchemaSource;
import com.hayden.graphql.models.visitor.VisitorModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * Add a DataFetcher to the context so that it will be available in the RemoteDataFetcher provided by the client
 * service. {@see com.hayden.gateway.graphql.GraphQlDataFederation}
 * @param serviceId the serviceId of this graph-ql service. This will be the same for deploying multiple services of
 *                  the same type.
 * @param source the GraphQlSource schema provided.
 * @param federationSource the source code for the params fetchers so this service id can be federated.
 */
@Slf4j
public record GraphQlDataFederationModel(FederatedGraphQlServiceItemId serviceId,
                                         Collection<GraphQlFederatedSchemaSource> source,
                                         Collection<DataFederationSources> federationSource)
        implements VisitorModel {
}
