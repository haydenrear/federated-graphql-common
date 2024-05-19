package com.hayden.graphql.models.visitor.datafetcher;

import com.hayden.graphql.models.visitor.model.DataSource;

import java.util.Map;

/**
 * The params is pulled from the service, compiled and then can use the queries.
 *
 * @param sources  The source code for the params fetcher that will be compiled
 * @param typeName The base type name for the sources.
 */
public record DataFetcherGraphQlSource(String typeName, Map<DataFetcherSourceId, DataSource> sources) {
}
