package com.hayden.graphql.federated.transport.fetcher_transport;

import com.hayden.graphql.federated.FederatedGraphQlSourceProvider;
import com.hayden.graphql.models.federated.execution.FederatedExecutionResult;
import graphql.ErrorClassification;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.graphql.*;
import org.springframework.graphql.client.GraphQlTransport;
import org.springframework.graphql.support.DefaultExecutionGraphQlRequest;
import org.springframework.graphql.support.DefaultExecutionGraphQlResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AlternativeJdkIdGenerator;
import org.springframework.util.IdGenerator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * The transport that uses the DGS context. When a new DataFetcher is compiled and registered with DGS, this
 * will be available through this transport.
 */
@Component
@RequiredArgsConstructor
public class FetcherGraphQlTransport implements GraphQlTransport {

    private final FederatedGraphQlSourceProvider federatedGraphQlSource;

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
                        var errorsRes = parseError(response);
                        return Flux.from(
                                        Optional.<Flux<ExecutionResult>>ofNullable(response.getData())
                                                .orElse(Flux.empty())
                                )
                                .map(executionResult -> new DefaultExecutionGraphQlResponse(response.getExecutionInput(), executionResult))
                                .concatWith(Flux.just(errorsRes));
                    } catch (AssertionError ex) {
                        throw new AssertionError(ex.getMessage() + "\nRequest: " + request, ex);
                    } catch (ClassCastException c) {
                        return Flux.just(toExecutionError(response));
                    }
                });
    }

    private static @NotNull DefaultExecutionGraphQlResponse toExecutionError(ExecutionGraphQlResponse response) {
        return new DefaultExecutionGraphQlResponse(
                response.getExecutionInput(),
                ExecutionResult.newExecutionResult().addError(
                                GraphQLError.newError()
                                        .errorType(ErrorClassification.errorClassification("Failure to cast response to subscription."))
                                        .extensions(response.getExecutionInput().getExtensions())
                                        .locations(new ArrayList<>())
                                        .path(new ArrayList<>())
                                        .message("Failure to cast response to subscription.")
                                        .build()
                        )
                        .build()
        );
    }

    private static @NotNull DefaultExecutionGraphQlResponse parseError(ExecutionGraphQlResponse response) {
        List<ResponseError> errors = response.getErrors();
        var errorsRes = new DefaultExecutionGraphQlResponse(response.getExecutionInput(),
                ExecutionResult.newExecutionResult().errors(
                                errors.stream()
                                        .map(e -> GraphQLError.newError()
                                                .errorType(e.getErrorType())
                                                .extensions(e.getExtensions())
                                                .locations(e.getLocations())
                                                .path(e.getParsedPath())
                                                .message(e.getMessage())
                                                .build()
                                        )
                                        .toList()
                        )
                        .build());
        return errorsRes;
    }


    private ExecutionGraphQlRequest toExecutionRequest(GraphQlRequest request) {
        return new DefaultExecutionGraphQlRequest(
                request.getDocument(),
                request.getOperationName(),
                request.getVariables(),
                request.getExtensions(),
                idGenerator.generateId().toString(),
                null
        );
    }
}
