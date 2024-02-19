package com.hayden.graphql.federated.transport;

import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import com.hayden.graphql.models.federated.response.DefaultClientGraphQlResponse;
import com.hayden.graphql.models.visitor.DataTemplate;
import lombok.AllArgsConstructor;
import lombok.experimental.Delegate;
import org.reactivestreams.Publisher;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlTransport;

public interface FederatedItemGraphQlTransport {
    Publisher<ClientGraphQlResponse> next(ClientFederatedRequestItem request);

    @AllArgsConstructor
    class GraphQlTransportDelegate implements FederatedItemGraphQlTransport {

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

}
