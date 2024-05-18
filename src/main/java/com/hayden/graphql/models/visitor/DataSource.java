package com.hayden.graphql.models.visitor;

import com.hayden.graphql.models.GraphQlTarget;

/**
 * Data fetcher can be provided as a file location or the source code as a string.
 *
 * @param dataFetcherSourceType
 * @param target
 */
public record DataSource(String id, SourceData sourceMetadata, DataTemplate dataTemplate) {
    public DataSource(String id, GraphQlTarget targetType, String target, DataTemplate dataTemplate) {
        this(id, new SourceData(id, target, targetType), dataTemplate);
    }
    public DataSource(String id, GraphQlTarget targetType, String target) {
        this(id, new SourceData(target, targetType), null);
    }
}
