package com.hayden.graphql.federated;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import com.hayden.graphql.models.client.ClientRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.ExecutionGraphQlRequest;
import org.springframework.graphql.ExecutionGraphQlResponse;
import org.springframework.graphql.ExecutionGraphQlService;
import reactor.core.publisher.Mono;

public interface FederatedExecutionGraphQlService extends ExecutionGraphQlService {

    Mono<ExecutionGraphQlResponse> execute(ClientRequest clientRequest);

    Mono<ExecutionGraphQlResponse> execute(ExecutionGraphQlRequest request, boolean parsed);

    FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient federatedClient();

}
