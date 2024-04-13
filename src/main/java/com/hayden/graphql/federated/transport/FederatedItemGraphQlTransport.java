package com.hayden.graphql.federated.transport;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilder;
import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import com.hayden.graphql.models.federated.response.DefaultClientGraphQlResponse;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import com.hayden.graphql.models.visitor.DataTemplate;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlTransport;
import org.springframework.stereotype.Component;

import java.util.Optional;

public interface FederatedItemGraphQlTransport<R extends GraphQlRequest> extends GraphQlTransport {

    Publisher<ClientGraphQlResponse> next(R request);

    default Optional<FederatedGraphQlServiceItemId> serviceItemId() {
        return Optional.empty();
    }


    /**
     * Multiple of these delegates are created, each one of them providing a different underlying GraphQlTransport, for
     * example to send GraphQl queries to other services. This is for when instead of providing a DataFetcher, the client
     * sends multiple queries with different MimeTypes, and the service is chosen based on the MimeType.
     */
    @AllArgsConstructor
    class GraphQlTransportDelegate implements FederatedItemGraphQlTransport<ClientFederatedRequestItem> {

        @Delegate
        private final GraphQlTransport graphQlTransport;
        private final Encoder<?> encoder;
        private final Decoder<?> decoder;
        private final FederatedGraphQlServiceItemId serviceItemId;

        @Override
        public Publisher<ClientGraphQlResponse> next(ClientFederatedRequestItem request) {
            return graphQlTransport.execute(request)
                    .map(g -> new DefaultClientGraphQlResponse(request, g, encoder, decoder));
        }

        @Override
        public Optional<FederatedGraphQlServiceItemId> serviceItemId() {
            return Optional.of(this.serviceItemId);
        }
    }

    /**
     * Delegate for FetcherGraphQlTransport. This is auto-updated through type registry in DGS, and so is a singleton. This is used
     * for when the data service provides a DataFetcher that calls out to them and then the query calls this data fetcher.
     */
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
