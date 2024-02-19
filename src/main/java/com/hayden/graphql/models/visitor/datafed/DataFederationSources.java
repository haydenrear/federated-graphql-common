package com.hayden.graphql.models.visitor.datafed;

import com.hayden.graphql.models.GraphQlTarget;
import com.hayden.graphql.models.SourceType;

/**
 * Data fetcher can be provided as a file location or the source code as a string.
 *
 * @param target
 */
public record DataFederationSources(GraphQlTarget targetType,
                                    String target,
                                    String packageName,
                                    String typeName,
                                    SourceType dataFetcherSourceType) {
}
