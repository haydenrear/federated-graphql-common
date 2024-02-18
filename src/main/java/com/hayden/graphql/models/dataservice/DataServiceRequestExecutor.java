package com.hayden.graphql.models.dataservice;

import graphql.schema.DataFetchingEnvironment;
import lombok.Builder;
import lombok.experimental.Delegate;
import org.reactivestreams.Publisher;
import org.springframework.graphql.ExecutionGraphQlRequest;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.lang.Nullable;

import java.net.http.HttpHeaders;
import java.util.Map;

public interface DataServiceRequestExecutor {

    record FederatedRequestData(RequestData[] data) {}

    @Builder
    record RequestData(String path,
                          @Delegate FederatedGraphQlService federatedService,
                          Map<String, String> queryParams,
                          Object requestBody,
                          DataFetchingEnvironment dataFetchingEnvironment,
                          HttpHeaders headers,
                          @Nullable String operationName,
                          Map<String, Object> variables,
                          Map<String, Object> extensions,
                          Map<String, Object> attributes,
                          Class<?> responseT,
                       ExecutionGraphQlRequest request) {}

    record FederatedGraphQlResponseItem(Object t, GraphQlResponse graphQlResponse) {}
    record FederatedGraphQlResponse(FederatedGraphQlResponseItem[] items) {}


    Publisher<FederatedGraphQlResponse> request(FederatedRequestData federatedRequestData);

}
