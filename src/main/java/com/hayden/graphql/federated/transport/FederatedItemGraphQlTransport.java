package com.hayden.graphql.federated.transport;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilder;
import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import com.hayden.graphql.models.federated.response.DefaultClientGraphQlResponse;
import com.hayden.graphql.models.visitor.DataTemplate;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlTransport;
import org.springframework.stereotype.Component;

import java.util.Optional;

public interface FederatedItemGraphQlTransport<R extends ClientGraphQlRequest> {
    Publisher<ClientGraphQlResponse> next(R request);

    @AllArgsConstructor
    class GraphQlTransportDelegate implements FederatedItemGraphQlTransport<ClientFederatedRequestItem> {

        @Delegate
        private final GraphQlTransport graphQlTransport;
        private final Encoder<?> encoder;
        private final Decoder<?> decoder;

        @Override
        public Publisher<ClientGraphQlResponse> next(ClientFederatedRequestItem request) {
            return graphQlTransport.execute(request)
                    .map(g -> new DefaultClientGraphQlResponse(request, g, encoder, decoder));
        }
    }

    @RequiredArgsConstructor
    @Component
    class FetcherGraphQlTransportDelegate implements FederatedItemGraphQlTransport<ClientGraphQlRequest> {

        @Delegate
        private final FetcherGraphQlTransport fetcherGraphQlTransport;
        @Autowired(required = false)
        @SuppressWarnings("rawtypes")
        private Encoder encoder;
        @Autowired(required = false)
        @SuppressWarnings("rawtypes")
        private Decoder decoder;

        @PostConstruct
        public void setValues() {
            encoder = Optional.ofNullable(encoder).orElse(FederatedGraphQlClientBuilder.DefaultJackson2Codecs.encoder());
            decoder = Optional.ofNullable(decoder).orElse(FederatedGraphQlClientBuilder.DefaultJackson2Codecs.decoder());
        }

        @Override
        public Publisher<ClientGraphQlResponse> next(ClientGraphQlRequest request) {
            return fetcherGraphQlTransport.execute(request)
                    .map(g -> new DefaultClientGraphQlResponse(request, g, encoder, decoder));
        }
    }

}
