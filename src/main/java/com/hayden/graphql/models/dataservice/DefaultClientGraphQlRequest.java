package com.hayden.graphql.models.dataservice;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.support.DefaultGraphQlRequest;
import org.springframework.lang.Nullable;

/**
 * Default implementation of {@link ClientGraphQlRequest}.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
final class DefaultClientGraphQlRequest extends DefaultGraphQlRequest implements ClientGraphQlRequest {

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();


    DefaultClientGraphQlRequest(
            String document,
            @Nullable String operationName,
            Map<String, Object> variables,
            Map<String, Object> extensions,
            Map<String, Object> attributes
    ) {

        super(document, operationName, variables, extensions);
        this.attributes.putAll(attributes);
    }


    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

}
