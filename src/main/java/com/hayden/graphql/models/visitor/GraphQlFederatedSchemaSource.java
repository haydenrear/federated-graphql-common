package com.hayden.graphql.models.visitor;

import com.hayden.graphql.models.GraphQlTarget;

/**
 * A services GraphQl schema to be proxied using GraphQl federation.
 *
 */
public record GraphQlFederatedSchemaSource(GraphQlTarget targetType, String target) {
}
