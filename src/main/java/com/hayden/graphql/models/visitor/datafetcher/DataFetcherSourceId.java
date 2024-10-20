package com.hayden.graphql.models.visitor.datafetcher;

import com.hayden.graphql.models.SourceType;
import org.springframework.util.MimeType;

/**
 * Each params fetcher has a field name and the source. The source can be provided in Java or query dsl, which will
 * then be compiled to Java classes and loaded into the program.
 *
 * @param dataFetcherSourceType
 * @param fieldName
 */
public record DataFetcherSourceId(
        String dataFetcherTypeName,
        SourceType dataFetcherSourceType,
        String dataFetchedClassName,
        String fieldName,
        MimeType mimeType
) {
}
