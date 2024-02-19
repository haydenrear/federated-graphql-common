package com.hayden.graphql.models.federated.response;


import lombok.experimental.Delegate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.ClientResponseField;
import org.springframework.graphql.client.FieldAccessException;


/**
 * Default implementation of {@link ClientGraphQlResponse}.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
public final class FederatedClientGraphQlResponse implements ClientGraphQlResponse {

    private final ClientGraphQlRequest request;

    @Delegate
    private final ClientGraphQlResponse response;

    private final Encoder<?> encoder;

    private final Decoder<?> decoder;


    FederatedClientGraphQlResponse(ClientGraphQlRequest request, ClientGraphQlResponse response,
                                   Encoder<?> encoder, Decoder<?> decoder) {
        this.request = request;
        this.response = response;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    public FederatedClientGraphQlResponse(ClientGraphQlRequest request, GraphQlResponse response,
                                          Encoder<?> encoder, Decoder<?> decoder) {

        this.request = request;
        this.response = new DefaultClientGraphQlResponse(request, response, encoder, decoder);
        this.encoder = encoder;
        this.decoder = decoder;
    }


    ClientGraphQlRequest getRequest() {
        return this.request;
    }

    Encoder<?> getEncoder() {
        return this.encoder;
    }

    Decoder<?> getDecoder() {
        return this.decoder;
    }

    @Override
    public <D> D toEntity(Class<D> type) {
        ClientResponseField field = field("");
        D entity = field.toEntity(type);

        // should never happen because toEntity checks response.isValid
        if (entity == null) {
            throw new FieldAccessException(getRequest(), this, field);
        }

        return entity;
    }

    @Override
    public <D> D toEntity(ParameterizedTypeReference<D> type) {
        ClientResponseField field = field("");
        D entity = field.toEntity(type);

        // should never happen because toEntity checks response.isValid
        if (entity == null) {
            throw new FieldAccessException(getRequest(), this, field);
        }

        return entity;
    }

}
