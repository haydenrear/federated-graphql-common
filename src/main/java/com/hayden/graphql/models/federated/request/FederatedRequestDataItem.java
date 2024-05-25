package com.hayden.graphql.models.federated.request;

import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceFetcherItemId;
import graphql.ExecutionInput;
import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import lombok.Builder;
import lombok.experimental.Delegate;
import org.springframework.graphql.ExecutionGraphQlRequest;
import org.springframework.lang.Nullable;

import java.net.http.HttpHeaders;
import java.util.Map;

@Builder
public record FederatedRequestDataItem(
        String path,
        @Nullable @Delegate FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceFetcherId federatedService,
        Map<String, String> queryParams,
        Object requestBody,
        DataFetchingEnvironment dataFetchingEnvironment,
        HttpHeaders headers,
        @Nullable String operationName,
        Map<String, Object> variables,
        Map<String, Object> extensions,
        Map<String, Object> attributes,
        Class<?> responseT,
        ExecutionGraphQlRequest request) {



}