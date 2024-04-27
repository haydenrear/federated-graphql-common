package com.hayden.graphql.federated.transport.federated_transport;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import com.hayden.graphql.federated.transport.fetcher_transport.FetcherGraphQlTransport;
import com.hayden.graphql.federated.transport.health.GraphQlTransportFailureAction;
import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import com.hayden.graphql.models.federated.response.DefaultClientGraphQlResponse;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.graphql.GraphQlRequest;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlTransport;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import reactor.core.publisher.Flux;


public interface FederatedItemGraphQlTransport<R extends GraphQlRequest> extends GraphQlTransport {

    Publisher<ClientGraphQlResponse> next(R request);

    default Publisher<GraphQlResponse> nextGraphQlResponse(R request) {
        return Flux.from(this.next(request));
    }

    default Optional<FederatedGraphQlServiceItemId> serviceItemId() {
        return Optional.empty();
    }


    /**
     * The graphQl transport that is accessed once the DataFetcher has been retrieved, one created for each instance
     * of each data service.
     */
    @AllArgsConstructor
    class GraphQlTransportDelegate implements FederatedItemGraphQlTransport<ClientFederatedRequestItem> {

        @Delegate
        private final GraphQlTransport graphQlTransport;
        private final Encoder<?> encoder;
        private final Decoder<?> decoder;
        private final FederatedGraphQlServiceItemId serviceItemId;
        private final List<GraphQlTransportFailureAction> failures;

        @Override
        public Publisher<ClientGraphQlResponse> next(ClientFederatedRequestItem request) {
            return graphQlTransport.execute(request)
                    .doOnError(t -> failures.stream().filter(f -> f.matches(t))
                            .forEach(GraphQlTransportFailureAction::failureEvent)
                    )
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
            encoder = Optional.ofNullable(encoder).orElse(FederatedGraphQlClientBuilderHolder.DefaultJackson2Codecs.encoder());
            decoder = Optional.ofNullable(decoder).orElse(FederatedGraphQlClientBuilderHolder.DefaultJackson2Codecs.decoder());
        }

        @Override
        public Publisher<ClientGraphQlResponse> next(ClientGraphQlRequest request) {
            return fetcherGraphQlTransport.execute(request)
                    .map(g -> new DefaultClientGraphQlResponse(request, g, encoder, decoder));
        }
    }

}
