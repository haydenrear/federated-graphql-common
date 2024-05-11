package com.hayden.graphql.federated.execution;

import com.hayden.graphql.models.federated.execution.FederatedExecutionResult;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import lombok.experimental.Delegate;
import org.reactivestreams.Publisher;
import org.springframework.graphql.client.ClientGraphQlResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DataServiceRequestExecutor {

    record FederatedGraphQlResponseItem(Object t,
                                        @Delegate ClientGraphQlResponse graphQlResponse) implements ExecutionResult, ClientGraphQlResponse {

        @Override
        public boolean isDataPresent() {
            return t != null && graphQlResponse.getData() != null;
        }

        @Override
        public Map<String, Object> toSpecification() {
            return new HashMap<>();
        }

    }

    record FederatedGraphQlResponse(FederatedGraphQlResponseItem[] items) {
        public FederatedExecutionResult toResult() {
            return new FederatedExecutionResult(items);
        }
    }


    Publisher<FederatedGraphQlResponse> request(FederatedRequestData federatedRequestData);

}
