package com.hayden.graphql.models.dataservice;


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
final class DefaultClientGraphQlResponse extends ResponseMapGraphQlResponse implements ClientGraphQlResponse {

    private final ClientGraphQlRequest request;

    private final Encoder<?> encoder;

    private final Decoder<?> decoder;


    DefaultClientGraphQlResponse(
            ClientGraphQlRequest request, GraphQlResponse response, Encoder<?> encoder, Decoder<?> decoder) {

        super(response);

        this.request = request;
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
    public ClientResponseField field(String path) {
        return new DefaultClientResponseField(this, super.field(path));
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
