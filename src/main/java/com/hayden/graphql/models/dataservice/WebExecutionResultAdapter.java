package com.hayden.graphql.models.dataservice;

import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQLError;
import lombok.experimental.UtilityClass;
import org.springframework.graphql.ResponseError;

@UtilityClass
public class WebExecutionResultAdapter {

    public <T> ExecutionResult executionResult(DataServiceRequestExecutor.FederatedGraphQlResponse g) {
        return new ExecutionResultImpl(
                g.graphQlResponse().getData(),
                g.graphQlResponse().getErrors().stream()
                        .map(WebExecutionResultAdapter::toGraphQlError)
                        .toList(),
                g.graphQlResponse().getExtensions()
        );
    }

    private static GraphQLError toGraphQlError(ResponseError r) {
        return GraphQLError.newError()
                .message(r.getMessage())
                .extensions(r.getExtensions())
                .errorType(r.getErrorType())
                .locations(r.getLocations())
                .path(r.getParsedPath())
                .build();
    }

}
