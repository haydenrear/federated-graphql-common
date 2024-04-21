package com.hayden.graphql.federated.transport.health;

import lombok.AllArgsConstructor;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.net.ConnectException;
import java.util.function.Supplier;

@AllArgsConstructor
public
class UnregisterGraphQlTransportFailureAction implements GraphQlTransportFailureAction {

    Supplier<String> failureCallable;


    @Override
    public void failureEvent() {
        failureCallable.get();
    }

    @Override
    public boolean matches(Throwable throwable) {
        return switch (throwable) {
            case WebClientResponseException w ->
                    w.getStatusCode().is4xxClientError() || w.getStatusCode().is5xxServerError();
            case ConnectException io -> true;
            case IOException io -> true;
            default -> false;
        };
    }
}
