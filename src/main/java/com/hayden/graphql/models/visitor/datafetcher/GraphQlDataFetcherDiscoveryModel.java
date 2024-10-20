package com.hayden.graphql.models.visitor.datafetcher;

import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceFetcherItemId;
import com.hayden.graphql.models.visitor.model.DataTemplate;
import com.hayden.graphql.models.visitor.model.VisitorModel;
import com.hayden.graphql.models.visitor.schema.GraphQlFederatedSchemaSource;
import graphql.schema.DataFetcher;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        Collection<DataFetcherGraphQlSource> fetcherSource,
        boolean invalidateCurrent
) implements VisitorModel {

    @Override
    public FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceInstanceId id() {
        return serviceId.serviceInstanceId();
    }

    public record DataFetcherMetaData(Class<? extends DataFetcher<?>> fetcher, DataTemplate template)  {
        public DataFetcherMetaData(Class<? extends DataFetcher<?>> fetcher) {
            this(fetcher, null);
        }
    }

    public record DataFetcherData(DataFetcher<?> fetcher, DataTemplate template, List<DataFetcherAssignment> field)  {

        public record DataFetcherAssignment(String parentType, String field) {}

        public DataFetcherData(DataFetcher<?> fetcher, DataTemplate dataTemplate) {
            this(fetcher, dataTemplate, new ArrayList<>());
        }

        public DataFetcherData(DataFetcher<?> fetcher) {
            this(fetcher, null, new ArrayList<>());
        }
    }

}
