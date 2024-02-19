package com.hayden.graphql.models.visitor.datafetcher;

import com.hayden.graphql.models.GraphQlTarget;

import java.util.List;
import java.util.Map;

public record Sources(
        Map<GraphQlTarget, List<String>> targets) {
}
