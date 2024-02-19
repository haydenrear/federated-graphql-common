package com.hayden.graphql.models.visitor;

import com.hayden.graphql.models.GraphQlTarget;

/**
 * Data fetcher can be provided as a file location or the source code as a string.
 *
 * @param dataFetcherSourceType
 * @param target
 */
public record DataSource(SourceData sourceMetadata) {
    public DataSource(GraphQlTarget targetType, String target) {
        this(new SourceData(target, targetType));
    }
}
