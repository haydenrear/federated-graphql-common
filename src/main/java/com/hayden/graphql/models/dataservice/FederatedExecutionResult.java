package com.hayden.graphql.models.dataservice;

import com.hayden.graphql.models.client.ClientResponse;
import com.hayden.utilitymodule.MapFunctions;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import org.apache.commons.lang3.NotImplementedException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FederatedExecutionResult implements ExecutionResult {

    private ExecutionResult[] delegators;

    @Override
    public List<GraphQLError> getErrors() {
        return Arrays.stream(delegators).flatMap(e -> e.getErrors().stream())
                .toList();
    }

    @Override
    public <T> T getData() {
        throw new NotImplementedException("not implemented");
    }

    public ClientResponse getFederatedData() {
        return new ClientResponse(
                Arrays.stream(delegators)
                        .map(e -> new ClientResponse.ClientResponseItem(e.getData()))
                        .toArray(ClientResponse.ClientResponseItem[]::new)
        );
    }

    @Override
    public boolean isDataPresent() {
        return Arrays.stream(delegators).anyMatch(ExecutionResult::isDataPresent);
    }

    @Override
    public Map<Object, Object> getExtensions() {
        return MapFunctions.CollectMap(Arrays.stream(delegators).flatMap(e -> e.getExtensions().entrySet().stream()));
    }

    @Override
    public Map<String, Object> toSpecification() {
        return MapFunctions.CollectMap(Arrays.stream(delegators).flatMap(e -> e.toSpecification().entrySet().stream()));
    }
}
