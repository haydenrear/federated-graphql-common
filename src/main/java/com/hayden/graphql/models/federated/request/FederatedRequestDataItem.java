package com.hayden.graphql.models.federated.request;

import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import graphql.schema.DataFetchingEnvironment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.graphql.ExecutionGraphQlRequest;
import org.springframework.lang.Nullable;

import java.net.http.HttpHeaders;
import java.util.Map;

@Builder
public record FederatedRequestDataItem(
        String path,
        @Delegate FederatedGraphQlServiceItemId.FederatedGraphQlServiceId federatedService,
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