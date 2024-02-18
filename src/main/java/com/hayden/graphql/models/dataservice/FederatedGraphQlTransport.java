package com.hayden.graphql.models.dataservice;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.client.GraphQlTransport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;


@RequiredArgsConstructor
public class FederatedGraphQlTransport implements GraphQlTransport {

    private final Map<FederatedGraphQlService, GraphQlTransport> transports;

    public Optional<GraphQlTransport> retrieve(GraphQlRequest request) {
        if (request instanceof FederatedGraphQlRequest.FederatedClientGraphQlRequestItem federated) {
            return Optional.ofNullable(transports.get(federated.service()));
        }

        return Optional.empty();
    }

    @Override
    public Mono<GraphQlResponse> execute(GraphQlRequest request) {
        return Mono.justOrEmpty(retrieve(request)).flatMap(e -> e.execute(request));
    }

    @Override
    public Flux<GraphQlResponse> executeSubscription(GraphQlRequest request) {
        return Mono.justOrEmpty(retrieve(request)).flatMapMany(e -> e.executeSubscription(request));
    }
}
