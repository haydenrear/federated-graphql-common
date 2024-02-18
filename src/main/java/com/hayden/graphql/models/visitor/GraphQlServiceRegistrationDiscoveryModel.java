package com.hayden.graphql.models.visitor;

import graphql.schema.DataFetcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MimeType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO: create a request for delete of schema from TypeRegistry.
 * @param serviceId the serviceId of this graph-ql service. This will be the same for deploying multiple services of
 *                  the same type.
 * @param source the GraphQlSource schema provided.
 * @param fetcherSource the source code for the params fetchers so this service id can be federated.
 */
@Slf4j
public record GraphQlServiceRegistrationDiscoveryModel(String serviceId,
                                                       GraphQlFederatedSchemaSource source,
                                                       Collection<GraphQlFetcherSource> fetcherSource) implements VisitorModel {

    public enum GraphQlTarget {
        FileLocation, String
    }

    public enum DataFetcherSourceType {
        GraphQl, DgsComponentJava, DataFetcherJava
    }

    /**
     * The params is pulled from the service, compiled and then can use the queries.
     * @param sources The source code for the params fetcher that will be compiled
     * @param typeName The base type name for the sources.
     */
    public record GraphQlFetcherSource(String typeName, Map<DataFetcherSourceId, DataFetcherSource> sources, DataFetcherTemplate template) {}

    /**
     * A services GraphQl schema to be proxied using GraphQl federation.
     * @param sources
     */
    public record GraphQlFederatedSchemaSource(
            Sources sources) {
        public GraphQlFederatedSchemaSource(Map<GraphQlTarget, List<String>> targets) {
            this(new Sources(targets));
        }
    }

    public record DataFetcherTemplate(String query, Set<String> required, Set<String> optional, Map<String, Object> vars) {
        public String toQuery(Map<String, String> params) {
            String queryCreated = query;
            for (var p : params.entrySet()) {
                queryCreated = queryCreated.replace("{%s}".formatted(p.getKey()), p.getValue());
            }
            return queryCreated;
        }
    }

    public record DataFetcherMetaData(Class<? extends DataFetcher<?>> fetcher, DataFetcherTemplate template)  {}

    public record DataFetcherData(DataFetcher<?> fetcher, DataFetcherTemplate template)  {}

    public record Sources(Map<GraphQlTarget, List<String>> targets) {}

    /**
     * @param packageName
     */
    public record SourceMetadata(String packageName, String target, GraphQlTarget targetType) {
        public SourceMetadata(String target, GraphQlTarget targetType) {
            this(null, target, targetType);
        }
    }

    /**
     * Data fetcher can be provided as a file location or the source code as a string.
     * @param dataFetcherSourceType
     * @param target
     */
    public record DataFetcherSource(SourceMetadata sourceMetadata) {
        public DataFetcherSource(GraphQlTarget targetType, String target) {
            this(new SourceMetadata(target, targetType));
        }
    }

    /**
     * Each params fetcher has a field name and the source. The source can be provided in Java or query dsl, which will
     * then be compiled to Java classes and loaded into the program.
     * @param dataFetcherSourceType
     * @param fieldName
     */
    public record DataFetcherSourceId(DataFetcherSourceType dataFetcherSourceType, String fieldName, MimeType mimeType) {}
}
