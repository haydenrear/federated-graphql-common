package com.hayden.graphql.federated.transport.federated_transport;

import com.google.common.collect.Lists;
import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import com.hayden.graphql.federated.config.FederatedGraphQlProperties;
import com.hayden.graphql.federated.error.GraphQlTransportError;
import com.hayden.graphql.federated.transport.health.EmitFailureEventFailureAction;
import com.hayden.graphql.federated.transport.health.HealthEvent;
import com.hayden.graphql.federated.transport.health.UnregisterGraphQlTransportFailureAction;
import com.hayden.graphql.federated.transport.register.GraphQlRegistration;
import com.hayden.graphql.models.federated.response.DefaultClientGraphQlResponse;
import com.hayden.utilitymodule.MapFunctions;
import com.hayden.utilitymodule.result.error.Error;
import com.hayden.utilitymodule.result.Result;
import graphql.ErrorClassification;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import com.hayden.graphql.models.federated.request.FederatedGraphQlRequest;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceFetcherItemId;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.graphql.*;
import org.springframework.graphql.client.AbstractGraphQlClientBuilder;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.graphql.support.DefaultExecutionGraphQlResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;


@RequiredArgsConstructor
@Service
@Slf4j
public class FederatedGraphQlTransport implements FederatedItemGraphQlTransport<FederatedGraphQlRequest> {

    protected static final boolean jackson2Present = ClassUtils.isPresent(
            "com.fasterxml.jackson.databind.ObjectMapper", AbstractGraphQlClientBuilder.class.getClassLoader());

    private final Map<FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceFetcherId, List<FederatedItemGraphQlTransport<? extends ClientGraphQlRequest>>> transports
            = new ConcurrentHashMap<>();

    private final Map<String, List<FederatedGraphQlServiceFetcherItemId>> transportsIndex = new ConcurrentHashMap<>();

    private final CallDataFetchersFederatedGraphQlTransport fetcherGraphQlTransport;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final FederatedGraphQlProperties federatedGraphQlProperties;

    private Encoder<?> jsonEncoder;
    private Decoder<?> jsonDecoder;

    @Override
    public Publisher<ClientGraphQlResponse> next(FederatedGraphQlRequest request) {
        return Flux.fromStream(request.delegators().entrySet().stream())
                .flatMap(to -> {
                    FederatedGraphQlRequest.FederatedClientGraphQlRequestItem value = to.getValue();
                    return getCastTransport(value)
                            .doOnError(error -> log.error("Error when retrieving graphql transport: {}.", error))
                            .map(g -> g.next(value))
                            .orElse(Flux.just(graphQlTransportErrorResponse(request)));
                });
    }

    @Override
    public @NotNull Mono<GraphQlResponse> execute(@NotNull GraphQlRequest request) {
        return executeSubscription(request)
                .buffer(Duration.ofMillis(federatedGraphQlProperties.getTimeoutMillis()))
                .next()
                .map(g -> new DefaultExecutionGraphQlResponse(
                        ExecutionInput.newExecutionInput()
                                .query(request.getDocument())
                                .operationName(request.getOperationName())
                                .extensions(request.getExtensions())
                                .variables(request.getVariables())
                                .graphQLContext(request.toMap())
                                .build(),
                        ExecutionResult.newExecutionResult()
                                .errors(g.stream().flatMap(g1 -> g1.getErrors().stream()).map(r -> GraphQLError.newError().build()).toList())
                                .extensions(MapFunctions.CollectMap(g.stream().flatMap(g1 -> g1.getExtensions().entrySet().stream())))
                                .data(g.stream().map(GraphQlResponse::getData).toList())
                                .build()
                ));
    }

    @Override
    public @NotNull Flux<GraphQlResponse> executeSubscription(@NotNull GraphQlRequest request) {
        return doExecute(request);
    }

    private Flux<GraphQlResponse> doExecute(@NotNull GraphQlRequest request) {
        return Mono.justOrEmpty(getCastTransport(request))
                .flatMapMany(e -> Flux.just(
                        e.doOnError(error -> log.error("Error when retrieving graphql transport: {}.", error))
                                .map(g -> g.nextGraphQlResponse(request))
                                .orElse(Flux.just(graphQlTransportErrorResponse(request)))
                ))
                // in the case where a service is failing, it will be removed.
                .retryWhen(Retry.backoff(5, Duration.ofMillis(500)))
                .cast(GraphQlResponse.class);
    }

    @PostConstruct
    void setEncoders() {
        if (jackson2Present) {
            this.jsonEncoder = (this.jsonEncoder == null ? FederatedGraphQlClientBuilderHolder.DefaultJackson2Codecs.encoder() : this.jsonEncoder);
            this.jsonDecoder = (this.jsonDecoder == null ? FederatedGraphQlClientBuilderHolder.DefaultJackson2Codecs.decoder() : this.jsonDecoder);
        }
    }

    public @Nullable FederatedItemGraphQlTransport<? extends GraphQlRequest> transport(GraphQlRequest request) {
        return switch(request) {
            case FederatedGraphQlRequest.FederatedClientGraphQlRequestItem item -> mapTo(item.service())
                    .orElseGet(() -> {
                        log.error("Error when attempting to get transport for {}. Could not find transport for service.", item.service());
                        return null;
                    });
            case GraphQlRequest ignored -> this.fetcherGraphQlTransport;
        };
    }

    public Optional<FederatedItemGraphQlTransport<? extends GraphQlRequest>> mapTo(FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceFetcherId serviceId) {
        return Optional.ofNullable(transports.get(serviceId))
                .stream()
                .flatMap(Collection::stream)
                .findFirst()
                .map(f -> f);
    }

    public BiConsumer<String, FederatedGraphQlServiceFetcherItemId> register(GraphQlRegistration federatedGraphQlTransport) {
        Optional.ofNullable(switch (federatedGraphQlTransport) {
                    case GraphQlRegistration.GraphQlTransportFederatedGraphQlRegistration g ->
                            Pair.of(transportDelegate(g), g.id());
                    case GraphQlRegistration.FederatedItemFederatedGraphQlTransportRegistration g ->
                            Pair.of(g.graphQlTransport(), g.id());
                    default -> throw new NotImplementedException("%s did not exist.".formatted(federatedGraphQlTransport.getClass().getSimpleName()));
                })
                .ifPresent(g -> {
                    if (!this.transports.containsKey(g.getRight().id())) {
                        FederatedItemGraphQlTransport<ClientGraphQlRequest> left = getFederatedItemGraphQlTransport(g);
                        registerGraphQlTransport(federatedGraphQlTransport, left, g.getRight());
                    } else {
                        log.error("Attempted to re-register transport for particular service instance ID.");
                    }
                });

        return this::unregister;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static FederatedItemGraphQlTransport<ClientGraphQlRequest> getFederatedItemGraphQlTransport(
            Pair<? extends FederatedItemGraphQlTransport<? extends ClientGraphQlRequest>, FederatedGraphQlServiceFetcherItemId> g
    ) {
        if  (g.getLeft() instanceof FederatedItemGraphQlTransport transport) {
            return transport;
        }

        return null;
    }

    private @NotNull FederatedItemGraphQlTransport.FederatedTransportsGraphQlTransportDelegate transportDelegate(GraphQlRegistration.GraphQlTransportFederatedGraphQlRegistration g) {
        return new FederatedTransportsGraphQlTransportDelegate(
                g.graphQlTransport(), jsonEncoder, jsonDecoder, g.id(),
                Lists.newArrayList(
                        new UnregisterGraphQlTransportFailureAction(() -> unregister(g.id().serviceInstanceId().host().host(), g.id())),
                        new EmitFailureEventFailureAction(() -> publishFailEvent(g))
                )
        );
    }

    private String publishFailEvent(GraphQlRegistration.GraphQlTransportFederatedGraphQlRegistration g) {
        applicationEventPublisher.publishEvent(new HealthEvent.FailedHealthEvent(g.id()));
        return "Fail Event for %s".formatted(g.id());
    }

    public String unregister(String id, FederatedGraphQlServiceFetcherItemId serviceId) {
        this.transports.computeIfPresent(serviceId.id(), (key, prev) -> {
            prev.removeIf(f -> f.serviceItemId().filter(s -> s.equals(serviceId)).isPresent());
            return prev;
        });
        this.transportsIndex.computeIfPresent(serviceId.id().serviceId().serviceId(), (key, prev) -> {
            prev.removeIf(f -> f.equals(serviceId));
            return prev;
        });
        return id;
    }

    private Result<FederatedItemGraphQlTransport<GraphQlRequest>, Error> getCastTransport(@NotNull GraphQlRequest request) {
        return Optional.ofNullable(this.transport(request))
                .map(Result::ok)
                .orElse(Result.err("Error retrieving"))
                .flatMap(f -> f instanceof FederatedItemGraphQlTransport<?> t
                        ? Result.ok(t)
                        : Result.emptyError()
                )
                .cast();
    }

    private void registerGraphQlTransport(GraphQlRegistration federatedGraphQlTransport,
                                          FederatedItemGraphQlTransport<ClientGraphQlRequest> transport,
                                          FederatedGraphQlServiceFetcherItemId serviceItemId) {
        registerTransport(federatedGraphQlTransport, transport);
        registerIndex(federatedGraphQlTransport, serviceItemId);
    }


    private void registerIndex(GraphQlRegistration federatedGraphQlTransport,
                               FederatedGraphQlServiceFetcherItemId serviceItemId) {
        this.transportsIndex.compute(federatedGraphQlTransport.id().id().serviceId().serviceId(), (key, prev) -> {
            if (prev == null)
                prev = new ArrayList<>();

            prev.add(serviceItemId);
            return prev;
        });
    }

    private void registerTransport(GraphQlRegistration federatedGraphQlTransport,
                                   FederatedItemGraphQlTransport<ClientGraphQlRequest> transport) {
        this.transports.compute(federatedGraphQlTransport.id().id(), (key, prev) -> {
            if (prev == null)
                prev = new ArrayList<>();

            prev.add(transport);
            return prev;
        });
    }

    private GraphQlResponse graphQlTransportErrorResponse(@NotNull GraphQlRequest request) {
        return new DefaultExecutionGraphQlResponse(
                ExecutionInput.newExecutionInput()
                        .query(request.getDocument())
                        .operationName(request.getOperationName())
                        .graphQLContext(request.toMap())
                        .extensions(request.getExtensions())
                        .variables(request.getVariables())
                        .query(request.getDocument())
                        .build(),
                ExecutionResult.newExecutionResult()
                        .addError(new GraphQlTransportError("Error retrieving GraphQl Transport. Please try again."))
                        .build()
        );
    }

    private ClientGraphQlResponse graphQlTransportErrorResponse(@NotNull ClientGraphQlRequest request) {
        return new DefaultClientGraphQlResponse(
                request,
                new DefaultExecutionGraphQlResponse(
                        ExecutionInput.newExecutionInput()
                                .query(request.getDocument())
                                .operationName(request.getOperationName())
                                .graphQLContext(request.toMap())
                                .extensions(request.getExtensions())
                                .variables(request.getVariables())
                                .build(),
                        ExecutionResult.newExecutionResult()
                                .addError(new GraphQlTransportError("Error retrieving GraphQl Transport. Please try again."))
                                .build()
                ),
                jsonEncoder,
                jsonDecoder
        );
    }

}
