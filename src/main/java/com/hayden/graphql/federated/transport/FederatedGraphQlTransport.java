package com.hayden.graphql.federated.transport;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilder;
import com.hayden.graphql.models.visitor.DataTemplate;
import jakarta.annotation.PostConstruct;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import com.hayden.graphql.models.federated.request.FederatedGraphQlRequest;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.client.AbstractGraphQlClientBuilder;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.GraphQlTransport;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


@RequiredArgsConstructor
@Service
public class FederatedGraphQlTransport implements GraphQlTransport {

    protected static final boolean jackson2Present = ClassUtils.isPresent(
            "com.fasterxml.jackson.databind.ObjectMapper", AbstractGraphQlClientBuilder.class.getClassLoader());

    @SuppressWarnings("rawtypes")
    private final Map<FederatedGraphQlServiceItemId, FederatedItemGraphQlTransport> transports = new ConcurrentHashMap<>();

    private final FederatedItemGraphQlTransport.FetcherGraphQlTransportDelegate fetcherGraphQlTransport;

    private Encoder<?> jsonEncoder;
    private Decoder<?> jsonDecoder;

    public interface GraphQlRegistration {}

    public record GraphQlTransportRegistration(GraphQlTransport graphQlTransport,
                                               FederatedGraphQlServiceItemId serviceItemId,
                                               DataTemplate dataTemplate) implements GraphQlRegistration {}
    public record FederatedGraphQlTransportRegistration(FederatedItemGraphQlTransport<FederatedGraphQlRequest.FederatedClientGraphQlRequestItem> graphQlTransport,
                                                        FederatedGraphQlServiceItemId serviceItemId) implements GraphQlRegistration {}

    @PostConstruct
    void setEncoders() {
        if (jackson2Present) {
            this.jsonEncoder = (this.jsonEncoder == null ? FederatedGraphQlClientBuilder.DefaultJackson2Codecs.encoder() : this.jsonEncoder);
            this.jsonDecoder = (this.jsonDecoder == null ? FederatedGraphQlClientBuilder.DefaultJackson2Codecs.decoder() : this.jsonDecoder);
        }
    }


    @SuppressWarnings("unchecked")
    public Optional<FederatedItemGraphQlTransport<ClientGraphQlRequest>> retrieve(GraphQlRequest request) {
        if (request instanceof FederatedGraphQlRequest.FederatedClientGraphQlRequestItem federated) {
            return Optional.ofNullable(transports.get(federated.service()));
        }  else {
            return Optional.of(fetcherGraphQlTransport);
        }
    }

    public void register(GraphQlRegistration  federatedGraphQlTransport) {
        Optional.ofNullable(switch (federatedGraphQlTransport) {
                    case GraphQlTransportRegistration g ->
                            Pair.of(new FederatedItemGraphQlTransport.GraphQlTransportDelegate(g.graphQlTransport, jsonEncoder, jsonDecoder), g.serviceItemId);
                    case FederatedGraphQlTransportRegistration g ->
                            Pair.of(g.graphQlTransport, g.serviceItemId);
                    default -> null;
                })
                .ifPresent(g -> this.transports.put(g.getRight(), g.getLeft()));
    }

    @Override
    public Mono<GraphQlResponse> execute(GraphQlRequest request) {
        throw new NotImplementedException("");
    }

    @Override
    public @NotNull Flux<GraphQlResponse> executeSubscription(@NotNull GraphQlRequest request) {
        return Mono.justOrEmpty(retrieve(request))
                .flatMapMany(e -> {
                    if (request instanceof ClientFederatedRequestItem item) {
                        return e.next(item);
                    }
                    return Flux.empty();
                });
    }
}
