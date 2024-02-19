package com.hayden.graphql.federated.transport.http;

import lombok.Builder;
import org.springframework.graphql.client.GraphQlTransport;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Builder
public record HttpGraphQlTransportBuilder(
        String host, String path, MultiValueMap<String, String> queryParams
) {
    public GraphQlTransport toTransport() {
        return new HttpGraphQlTransport(
                WebClient.builder()
                        .baseUrl(UriComponentsBuilder.fromPath(path).host(host).queryParams(queryParams).build().toUriString())
                        .build()
        );
    }
}
