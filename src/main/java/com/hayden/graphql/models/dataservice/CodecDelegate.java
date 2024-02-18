package com.hayden.graphql.models.dataservice;


import java.util.List;
import java.util.stream.Stream;

import org.springframework.core.ResolvableType;
import org.springframework.core.codec.Decoder;
import org.springframework.core.codec.Encoder;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.graphql.server.support.GraphQlWebSocketMessage;
import org.springframework.http.MediaType;
import org.springframework.http.codec.CodecConfigurer;
import org.springframework.http.codec.DecoderHttpMessageReader;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

/**
 * Helper class for encoding and decoding GraphQL messages.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
final class CodecDelegate {

    private static final ResolvableType MESSAGE_TYPE = ResolvableType.forClass(GraphQlWebSocketMessage.class);


    private final CodecConfigurer codecConfigurer;

    private final Decoder<?> decoder;

    private final Encoder<?> encoder;


    CodecDelegate(CodecConfigurer configurer) {
        Assert.notNull(configurer, "CodecConfigurer is required");
        this.codecConfigurer = configurer;
        this.decoder = findJsonDecoder(configurer);
        this.encoder = findJsonEncoder(configurer);
    }

    static Encoder<?> findJsonEncoder(CodecConfigurer configurer) {
        return findJsonEncoder(configurer.getWriters().stream()
                .filter(writer -> writer instanceof EncoderHttpMessageWriter)
                .map(writer -> ((EncoderHttpMessageWriter<?>) writer).getEncoder()));
    }

    static Decoder<?> findJsonDecoder(CodecConfigurer configurer) {
        return findJsonDecoder(configurer.getReaders().stream()
                .filter(reader -> reader instanceof DecoderHttpMessageReader)
                .map(reader -> ((DecoderHttpMessageReader<?>) reader).getDecoder()));
    }

    static Encoder<?> findJsonEncoder(List<Encoder<?>> encoders) {
        return findJsonEncoder(encoders.stream());
    }

    static Decoder<?> findJsonDecoder(List<Decoder<?>> decoders) {
        return findJsonDecoder(decoders.stream());
    }

    private static Encoder<?> findJsonEncoder(Stream<Encoder<?>> stream) {
        return stream
                .filter(encoder -> encoder.canEncode(MESSAGE_TYPE, MediaType.APPLICATION_JSON))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No JSON Encoder"));
    }

    private static Decoder<?> findJsonDecoder(Stream<Decoder<?>> decoderStream) {
        return decoderStream
                .filter(decoder -> decoder.canDecode(MESSAGE_TYPE, MediaType.APPLICATION_JSON))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No JSON Decoder"));
    }


    public CodecConfigurer getCodecConfigurer() {
        return this.codecConfigurer;
    }


    @SuppressWarnings("unchecked")
    public <T> WebSocketMessage encode(WebSocketSession session, GraphQlWebSocketMessage message) {

        DataBuffer buffer = ((Encoder<T>) this.encoder).encodeValue(
                (T) message, session.bufferFactory(), MESSAGE_TYPE, MimeTypeUtils.APPLICATION_JSON, null);

        return new WebSocketMessage(WebSocketMessage.Type.TEXT, buffer);
    }

    @SuppressWarnings("ConstantConditions")
    public GraphQlWebSocketMessage decode(WebSocketMessage webSocketMessage) {
        DataBuffer buffer = DataBufferUtils.retain(webSocketMessage.getPayload());
        return (GraphQlWebSocketMessage) this.decoder.decode(buffer, MESSAGE_TYPE, null, null);
    }

}
