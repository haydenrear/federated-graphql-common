package com.hayden.graphql.models.visitor;

import com.hayden.graphql.models.GraphQlTarget;

/**
 * @param packageName
 */
public record SourceData(String packageName, String target, GraphQlTarget targetType) {
    public SourceData(String target, GraphQlTarget targetType) {
        this( null, target, targetType);
    }
}
