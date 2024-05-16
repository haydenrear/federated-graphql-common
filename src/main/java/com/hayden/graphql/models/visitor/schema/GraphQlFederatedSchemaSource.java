package com.hayden.graphql.models.visitor.schema;

import com.hayden.graphql.models.GraphQlTarget;
import com.hayden.utilitymodule.RandomUtils;

/**
 * A services GraphQl schema to be proxied using GraphQl federation.
 *
 */
public record GraphQlFederatedSchemaSource(GraphQlTarget targetType, String target, String schemaName) {
    public GraphQlFederatedSchemaSource(GraphQlTarget targetType, String target) {
        this(targetType, target, "%s.graphql".formatted(RandomUtils.randomNumberString(10)));
    }
}
