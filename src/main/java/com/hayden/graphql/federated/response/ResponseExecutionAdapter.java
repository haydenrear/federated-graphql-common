package com.hayden.graphql.federated.response;

import com.hayden.graphql.federated.execution.DataServiceRequestExecutor;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import lombok.experimental.UtilityClass;
import org.springframework.graphql.ResponseError;

@UtilityClass
public class ResponseExecutionAdapter {

    public ExecutionResult executionResult(DataServiceRequestExecutor.FederatedGraphQlResponse g) {
        return g.toResult();
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
