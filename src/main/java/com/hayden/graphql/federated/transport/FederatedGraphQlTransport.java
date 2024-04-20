package com.hayden.graphql.federated.transport;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import com.hayden.graphql.federated.transport.health.HealthEvent;
import jakarta.annotation.PostConstruct;
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
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.client.AbstractGraphQlClientBuilder;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.ClientGraphQlResponse;
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
public class FederatedGraphQlTransport implements FederatedItemGraphQlTransport<FederatedGraphQlRequest> {

    protected static final boolean jackson2Present = ClassUtils.isPresent(
            "com.fasterxml.jackson.databind.ObjectMapper", AbstractGraphQlClientBuilder.class.getClassLoader());

    private final Map<FederatedGraphQlServiceItemId.FederatedGraphQlServiceId, List<FederatedItemGraphQlTransport<? extends ClientGraphQlRequest>>> transports = new ConcurrentHashMap<>();

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
                    return getCastTransport(value).next(value);
                });
    }

    @Override
    public @NotNull Mono<GraphQlResponse> execute(@NotNull GraphQlRequest request) {
        return executeSubscription(request).last();
    }

    @Override
    public @NotNull Flux<GraphQlResponse> executeSubscription(@NotNull GraphQlRequest request) {
        return Mono.justOrEmpty(getCastTransport(request))
                .flatMapMany(e -> Flux.from(e.next(request))
                        .doOnError(t -> e.serviceItemId()
                                .ifPresent(s -> applicationEventPublisher.publishEvent(new HealthEvent.FailedHealthEvent(s))))
                )
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

    public FederatedItemGraphQlTransport<? extends GraphQlRequest> transport(GraphQlRequest request) {
        return switch(request) {
            case FederatedGraphQlRequest ignored ->  this;
            case FederatedGraphQlRequest.FederatedClientGraphQlRequestItem item -> mapTo(item.service())
                    .orElse(null);
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
                            Pair.of(new FederatedItemGraphQlTransport.GraphQlTransportDelegate(g.graphQlTransport(), jsonEncoder, jsonDecoder, g.id()), g.id());
                    case GraphQlRegistration.FederatedItemFederatedGraphQlTransportRegistration g ->
                            Pair.of(g.graphQlTransport(), g.id());
                    default -> throw new NotImplementedException("%s did not exist.".formatted(federatedGraphQlTransport.getClass().getSimpleName()));
                })
                .ifPresent(g -> registerGraphQlTransport(federatedGraphQlTransport, g));

        return this::unregister;
    }

    public void unregister(String id, FederatedGraphQlServiceItemId serviceId) {
        this.transports.computeIfPresent(serviceId.id(), (key, prev) -> {
            prev.removeIf(f -> f.serviceItemId().filter(s -> s.equals(serviceId)).isPresent());
            return prev;
        });
        this.transportsIndex.get(serviceId.serviceId());
    }

    @SuppressWarnings("unchecked")
    private FederatedItemGraphQlTransport<GraphQlRequest> getCastTransport(@NotNull GraphQlRequest request) {
        return (FederatedItemGraphQlTransport<GraphQlRequest>) this.transport(request);
    }

    private void registerGraphQlTransport(GraphQlRegistration federatedGraphQlTransport, Pair<? extends FederatedItemGraphQlTransport<? extends ClientGraphQlRequest>, FederatedGraphQlServiceItemId> g) {
        registerTransport(federatedGraphQlTransport, g);
        registerIndex(federatedGraphQlTransport, g);
    }


    private void registerIndex(GraphQlRegistration federatedGraphQlTransport, Pair<? extends FederatedItemGraphQlTransport<? extends ClientGraphQlRequest>, FederatedGraphQlServiceItemId> g) {
        this.transportsIndex.compute(federatedGraphQlTransport.id().id().serviceId(), (key, prev) -> {
            if (prev == null)
                prev = new ArrayList<>();

            prev.add(g.getRight());
            return prev;
        });
    }

    private void registerTransport(GraphQlRegistration federatedGraphQlTransport, Pair<? extends FederatedItemGraphQlTransport<? extends ClientGraphQlRequest>, FederatedGraphQlServiceItemId> g) {
        this.transports.compute(federatedGraphQlTransport.id().id(), (key, prev) -> {
            if (prev == null)
                prev = new ArrayList<>();

            prev.add(g.getLeft());
            return prev;
        });
    }

}
