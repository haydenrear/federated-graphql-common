package com.hayden.graphql.models.visitor.datafetcher;

import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceFetcherItemId;
import com.hayden.graphql.models.visitor.*;
import com.hayden.graphql.models.visitor.schema.GraphQlFederatedSchemaSource;
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
public record GraphQlDataFetcherDiscoveryModel(
        FederatedGraphQlServiceFetcherItemId serviceId,

        Collection<GraphQlFederatedSchemaSource> source,
        Collection<DataFetcherGraphQlSource> fetcherSource) implements VisitorModel {


    public record DataFetcherMetaData(Class<? extends DataFetcher<?>> fetcher, DataTemplate template)  {
        public DataFetcherMetaData(Class<? extends DataFetcher<?>> fetcher) {
            this(fetcher, null);
        }
    }

    public record DataFetcherData(DataFetcher<?> fetcher, DataTemplate template)  {
        public DataFetcherData(DataFetcher<?> fetcher) {
            this(fetcher, null);
        }
    }

}
