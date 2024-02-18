package com.hayden.graphql.models.dataservice;


import java.util.Map;

import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.GraphQlTransport;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;


/**
 * Transport to execute GraphQL requests over HTTP via {@link WebClient}.
 *
 * <p>Supports only single-response requests over HTTP POST. For subscriptions,
 * see {@link WebSocketGraphQlTransport} and {@link RSocketGraphQlTransport}.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
final class HttpGraphQlTransport implements GraphQlTransport {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE =
            new ParameterizedTypeReference<Map<String, Object>>() {};

    // To be removed in favor of Framework's MediaType.APPLICATION_GRAPHQL_RESPONSE
    private static final MediaType APPLICATION_GRAPHQL_RESPONSE =
            new MediaType("application", "graphql-response+json");


    private final WebClient webClient;

    private final MediaType contentType;


    HttpGraphQlTransport(WebClient webClient) {
        Assert.notNull(webClient, "WebClient is required");
        this.webClient = webClient;
        this.contentType = initContentType(webClient);
    }

    private static MediaType initContentType(WebClient webClient) {
        HttpHeaders headers = new HttpHeaders();
        webClient.mutate().defaultHeaders(headers::putAll);
        MediaType contentType = headers.getContentType();
        return (contentType != null ? contentType : MediaType.APPLICATION_JSON);
    }


    @Override
    @SuppressWarnings("removal")
    public Mono<GraphQlResponse> execute(GraphQlRequest request) {
        return this.webClient.post()
                .contentType(this.contentType)
                .accept(MediaType.APPLICATION_JSON, APPLICATION_GRAPHQL_RESPONSE, MediaType.APPLICATION_GRAPHQL)
                .bodyValue(request.toMap())
                .attributes(attributes -> {
                    if (request instanceof ClientGraphQlRequest clientRequest) {
                        attributes.putAll(clientRequest.getAttributes());
                    }
                })
                .retrieve()
                .bodyToMono(MAP_TYPE)
                .map(ResponseMapGraphQlResponse::new);
    }

    @Override
    public Flux<GraphQlResponse> executeSubscription(GraphQlRequest request) {
        throw new UnsupportedOperationException("Subscriptions not supported over HTTP");
    }

}
