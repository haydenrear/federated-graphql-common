package com.hayden.graphql.federated.transport.federated_transport;

import com.google.common.collect.Lists;
import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import com.hayden.graphql.federated.error.GraphQlTransportError;
import com.hayden.graphql.federated.transport.health.EmitFailureEventFailureAction;
import com.hayden.graphql.federated.transport.health.HealthEvent;
import com.hayden.graphql.federated.transport.health.UnregisterGraphQlTransportFailureAction;
import com.hayden.graphql.federated.transport.register.GraphQlRegistration;
import com.hayden.graphql.models.federated.response.DefaultClientGraphQlResponse;
import com.hayden.utilitymodule.result.Result;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
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
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.graphql.*;
import org.springframework.graphql.client.AbstractGraphQlClientBuilder;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.ClientGraphQlResponse;
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
import java.util.stream.Stream;


@RequiredArgsConstructor
@Service
@Slf4j
public class FederatedGraphQlTransport implements FederatedItemGraphQlTransport<FederatedGraphQlRequest> {

    protected static final boolean jackson2Present = ClassUtils.isPresent(
            "com.fasterxml.jackson.databind.ObjectMapper", AbstractGraphQlClientBuilder.class.getClassLoader());

    private final Map<FederatedGraphQlServiceItemId.FederatedGraphQlServiceId, List<FederatedItemGraphQlTransport<? extends ClientGraphQlRequest>>> transports 
            = new ConcurrentHashMap<>();

    private final Map<String, List<FederatedGraphQlServiceItemId>> transportsIndex = new ConcurrentHashMap<>();

    private final FederatedItemGraphQlTransport.FetcherGraphQlTransportDelegate fetcherGraphQlTransport;
    private final ApplicationEventPublisher applicationEventPublisher;

    private Encoder<?> jsonEncoder;
    private Decoder<?> jsonDecoder;

    @Override
    public Publisher<ClientGraphQlResponse> next(FederatedGraphQlRequest request) {
        return Flux.fromStream(request.delegators().entrySet().stream())
                .flatMap(to -> {
                    FederatedGraphQlRequest.FederatedClientGraphQlRequestItem value = to.getValue();
                    return getCastTransport(value)
                            .mapError(error -> log.error("Error when retrieving graphql transport: {}.", error))
                            .map(g -> g.next(value))
                            .orElse(Flux.just(graphQlTransportErrorResponse(request)));
                });
    }

    @Override
    public @NotNull Mono<GraphQlResponse> execute(@NotNull GraphQlRequest request) {
        return executeSubscription(request).last();
    }

    @Override
    public @NotNull Flux<GraphQlResponse> executeSubscription(@NotNull GraphQlRequest request) {
        return doExecute(request);
    }

    private Flux<GraphQlResponse> doExecute(@NotNull GraphQlRequest request) {
        return Mono.justOrEmpty(getCastTransport(request))
                .flatMapMany(e -> Flux.just(
                        e.mapError(error -> log.error("Error when retrieving graphql transport: {}.", error))
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

    public Optional<FederatedItemGraphQlTransport<? extends GraphQlRequest>> mapTo(FederatedGraphQlServiceItemId.FederatedGraphQlServiceId serviceId) {
        return Optional.ofNullable(transports.get(serviceId))
                .stream()
                .flatMap(Collection::stream)
                .findFirst()
                .map(f -> f);
    }

    public BiConsumer<String, FederatedGraphQlServiceItemId> register(GraphQlRegistration federatedGraphQlTransport) {
        Optional.ofNullable(switch (federatedGraphQlTransport) {
                    case GraphQlRegistration.GraphQlTransportFederatedGraphQlRegistration g ->
                            Pair.of(transportDelegate(g), g.id());
                    case GraphQlRegistration.FederatedItemFederatedGraphQlTransportRegistration g ->
                            Pair.of(g.graphQlTransport(), g.id());
                    default -> throw new NotImplementedException("%s did not exist.".formatted(federatedGraphQlTransport.getClass().getSimpleName()));
                })
                .ifPresent(g -> {
                    FederatedItemGraphQlTransport<ClientGraphQlRequest> left = getFederatedItemGraphQlTransport(g);
                    registerGraphQlTransport(federatedGraphQlTransport, left, g.getRight());
                });

        return this::unregister;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static FederatedItemGraphQlTransport<ClientGraphQlRequest> getFederatedItemGraphQlTransport(
            Pair<? extends FederatedItemGraphQlTransport<? extends ClientGraphQlRequest>, FederatedGraphQlServiceItemId> g
    ) {
        if  (g.getLeft() instanceof FederatedItemGraphQlTransport transport) {
            return transport;
        }

        return null;
    }

    private @NotNull GraphQlTransportDelegate transportDelegate(GraphQlRegistration.GraphQlTransportFederatedGraphQlRegistration g) {
        return new GraphQlTransportDelegate(
                g.graphQlTransport(), jsonEncoder, jsonDecoder, g.id(),
                Lists.newArrayList(
                        new UnregisterGraphQlTransportFailureAction(() -> unregister(g.id().host(), g.id())),
                        new EmitFailureEventFailureAction(() -> publishFailEvent(g))
                )
        );
    }

    private String publishFailEvent(GraphQlRegistration.GraphQlTransportFederatedGraphQlRegistration g) {
        applicationEventPublisher.publishEvent(new HealthEvent.FailedHealthEvent(g.id()));
        return "Fail Event for %s".formatted(g.id());
    }

    public String unregister(String id, FederatedGraphQlServiceItemId serviceId) {
        this.transports.computeIfPresent(serviceId.id(), (key, prev) -> {
            prev.removeIf(f -> f.serviceItemId().filter(s -> s.equals(serviceId)).isPresent());
            return prev;
        });
        this.transportsIndex.computeIfPresent(serviceId.id().serviceId(), (key, prev) -> {
            prev.removeIf(f -> f.equals(serviceId));
            return prev;
        });
        return id;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Result<FederatedItemGraphQlTransport<GraphQlRequest>, Result.Error> getCastTransport(@NotNull GraphQlRequest request) {
        return Optional.ofNullable(this.transport(request))
                .map(Result::fromResult)
                .orElse(Result.fromError("Error retrieving"))
                .flatMap(f -> f instanceof FederatedItemGraphQlTransport t
                        ? Result.fromResult(t)
                        : Result.emptyError()
                );
    }

    private void registerGraphQlTransport(GraphQlRegistration federatedGraphQlTransport,
                                          FederatedItemGraphQlTransport<ClientGraphQlRequest> transport,
                                          FederatedGraphQlServiceItemId serviceItemId) {
        registerTransport(federatedGraphQlTransport, transport);
        registerIndex(federatedGraphQlTransport, serviceItemId);
    }


    private void registerIndex(GraphQlRegistration federatedGraphQlTransport,
                               FederatedGraphQlServiceItemId serviceItemId) {
        this.transportsIndex.compute(federatedGraphQlTransport.id().id().serviceId(), (key, prev) -> {
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
