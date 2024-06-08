package com.hayden.graphql.federated.client;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hayden.graphql.federated.interceptor.GraphQlFederatedInterceptor;
import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import com.hayden.graphql.models.federated.request.FederatedGraphQlRequest;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.graphql.client.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.util.Assert;

/**
 * Default, final {@link GraphQlClient} implementation for use with any transport.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
@Slf4j
public class DefaultFederatedGraphQlClient {

    private final GraphQlFederatedInterceptor.FederatedSubscriptionChain executeSubscriptionChain;
    private final ObjectMapper om;


    DefaultFederatedGraphQlClient(GraphQlFederatedInterceptor.FederatedSubscriptionChain executeSubscriptionChain, ObjectMapper om) {
        Assert.notNull(executeSubscriptionChain, "GraphQlClientInterceptor.SubscriptionChain is required");
        this.om = om;
        this.executeSubscriptionChain = executeSubscriptionChain;
    }

    public FederatedRequestSpec federatedDocuments(FederatedRequestData requestData,
                                                   FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs federatedGraphQlClient) {
        if (Arrays.stream(requestData.data()).anyMatch(f -> Objects.isNull(f.federatedService()))) {
            Assert.isTrue(Arrays.stream(requestData.data()).noneMatch(f -> Objects.nonNull(f.federatedService())),
                    "Federated request data invalid.");
            return new StitchingRequestSpect(requestData, federatedGraphQlClient, this.om);
        }

        return new FetcherRequestSpec(requestData, federatedGraphQlClient, this.om);
    }

    public interface FederatedRequestSpec {
        Flux<ClientGraphQlResponse> execute();
    }

    @RequiredArgsConstructor
    public final class FetcherRequestSpec implements FederatedRequestSpec {

        private final FederatedRequestData requestData;
        private final FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs client;
        private final ObjectMapper om;


        public Flux<ClientGraphQlResponse> execute() {
            return initRequest()
                    .flatMap(request -> executeSubscriptionChain.next(request)
                            .onErrorResume(
                                    ex -> !(ex instanceof GraphQlClientException),
                                    ex -> Mono.error(new GraphQlTransportException(ex, request))));
        }

        Flux<ClientGraphQlRequest> initRequest() {
            return Flux.fromArray(this.requestData.data())
                    .flatMap(document -> {
                        try {
                            return Mono.just(new ClientFederatedRequestItem(
                                    om.writeValueAsString(document.requestBody()),
                                    document.operationName(),
                                    document.variables(),
                                    document.extensions(),
                                    document.attributes(),
                                    requestData, client
                            ));
                        } catch (
                                JsonProcessingException e) {
                            return Mono.error(e);
                        }
                    });
        }

    }


    @RequiredArgsConstructor
    public final class StitchingRequestSpect implements FederatedRequestSpec  {

        private final FederatedRequestData requestData;
        private final FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs client;
        private final ObjectMapper om;


        public Flux<ClientGraphQlResponse> execute() {
            return initRequest()
                    .flatMapMany(request -> executeSubscriptionChain.next(request)
                    .onErrorResume(
                            ex -> !(ex instanceof GraphQlClientException),
                            ex -> Mono.error(new GraphQlTransportException(ex, request))));
        }

        Mono<ClientGraphQlRequest> initRequest() {
            return Flux.fromArray(this.requestData.data())
                    .flatMap(document -> {
                        return Mono.justOrEmpty(document.federatedService())
                                .flatMap(f -> {
                                    try {
                                        return Mono.just(Pair.of(document.federatedService(), new ClientFederatedRequestItem(
                                                om.writeValueAsString(document.requestBody()),
                                                document.operationName(),
                                                document.variables(),
                                                document.extensions(),
                                                document.attributes(),
                                                requestData, client
                                        )));
                                    } catch (JsonProcessingException e) {
                                        return Mono.error(e);
                                    }
                                })
                                .flux();
                    })
                    .doOnError(t -> log.error("{}", t.getMessage()))
                    .map(p -> Map.entry(p.getKey(), new FederatedGraphQlRequest.FederatedClientGraphQlRequestItem(p.getKey(), p.getRight())))
                    .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                    .map(FederatedGraphQlRequest::new);
        }

    }

}
