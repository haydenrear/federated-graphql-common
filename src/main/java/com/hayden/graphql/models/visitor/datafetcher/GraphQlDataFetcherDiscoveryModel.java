package com.hayden.graphql.models.visitor.datafetcher;

import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import com.hayden.graphql.models.visitor.*;
import graphql.schema.DataFetcher;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

/**
 * TODO: create a request for delete of schema from TypeRegistry.
 * @param serviceId the serviceId of this graph-ql service. This will be the same for deploying multiple services of
 *                  the same type.
 * @param source the GraphQlSource schema provided.
 * @param fetcherSource the source code for the params fetchers so this service id can be federated.
 */
@Slf4j
public record GraphQlDataFetcherDiscoveryModel(FederatedGraphQlServiceItemId serviceId,

                                               Collection<GraphQlFederatedSchemaSource> source,
                                               Collection<DataFetcherGraphQlSource> fetcherSource) implements VisitorModel {


    public record DataFetcherMetaData(Class<? extends DataFetcher<?>> fetcher, DataTemplate template)  {}

    public record DataFetcherData(DataFetcher<?> fetcher, DataTemplate template)  {}

}
