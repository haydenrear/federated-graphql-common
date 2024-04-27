package com.hayden.graphql.federated.transport.fetcher_transport;

import com.hayden.graphql.federated.FederatedGraphQlSourceProvider;
import graphql.ExecutionResult;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.*;
import org.springframework.graphql.client.GraphQlTransport;
import org.springframework.graphql.execution.DefaultExecutionGraphQlService;
import org.springframework.graphql.support.DefaultExecutionGraphQlRequest;
import org.springframework.graphql.support.DefaultExecutionGraphQlResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * The transport that uses the DGS context. When a new DataFetcher is compiled and registered with DGS, this
 * will be available through this transport.
 */
@Component
@RequiredArgsConstructor
public class FetcherGraphQlTransport implements GraphQlTransport {

    @Autowired
    private FederatedGraphQlSourceProvider federatedGraphQlSource;

    protected static final IdGenerator idGenerator = new AlternativeJdkIdGenerator();

    @Override
    public @NotNull Mono<GraphQlResponse> execute(@NotNull GraphQlRequest request) {
        return federatedGraphQlSource.executionGraphQlService()
                .execute(toExecutionRequest(request))
                .cast(GraphQlResponse.class);
    }

    @Override
    public @NotNull Flux<GraphQlResponse> executeSubscription(@NotNull GraphQlRequest request) {
        return federatedGraphQlSource.executionGraphQlService()
                .execute(toExecutionRequest(request))
                .flatMapMany(response -> {
                    try {
                        Object data = response.getData();

                        List<ResponseError> errors = response.getErrors();

                        return Flux.from((Publisher<ExecutionResult>) data).map(executionResult ->
                                new DefaultExecutionGraphQlResponse(response.getExecutionInput(), executionResult));
                    } catch (AssertionError ex) {
                        throw new AssertionError(ex.getMessage() + "\nRequest: " + request, ex);
                    }
                });
    }


    private ExecutionGraphQlRequest toExecutionRequest(GraphQlRequest request) {
        return new DefaultExecutionGraphQlRequest(
                request.getDocument(), request.getOperationName(), request.getVariables(), request.getExtensions(),
                idGenerator.generateId().toString(), null);
    }
}
