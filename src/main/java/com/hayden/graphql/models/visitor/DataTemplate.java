package com.hayden.graphql.models.visitor;

import java.util.Map;
import java.util.Set;

public record DataTemplate(String query,
                           Set<String> required,
                           Set<String> optional,
                           Map<String, Object> vars) {
    public String toQuery(Map<String, String> params) {
        String queryCreated = query;
        for (var p : params.entrySet()) {
            queryCreated = queryCreated.replace("{%s}".formatted(p.getKey()), p.getValue());
        }
        return queryCreated;
    }
}
