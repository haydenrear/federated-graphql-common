package com.hayden.graphql.models.dataservice;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import com.hayden.utilitymodule.result.Result;
import org.reactivestreams.Publisher;
import org.springframework.graphql.client.ClientGraphQlResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;

/**
 * Sends the request to the service, then that service publishes to a topic that is
 * listened to - so then the DataFetcher publishes the data that is received - to enable
 * scalable multicast.
 */
public abstract class CdcGraphQlDataFetcher<T, U, E> extends RemoteDataFederation<ClientGraphQlResponse> {

    abstract Result<T, E> fire(FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs federatedGraphQlClient,
                               FederatedRequestData data);

    abstract Publisher<U> subscribe();

    abstract Duration publisherTimeout();

    @Override
    Publisher<ClientGraphQlResponse> get(FederatedRequestData environment,
                                         FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs federatedGraphQlClient) {
        var f = fire(federatedGraphQlClient, environment);
        if (f.isOk()) {
            return Flux.from(subscribe())
                    .buffer(publisherTimeout())
                    .flatMap(t -> {
                        var found = this.from(t);
                        if (found.isOk())
                            return Mono.just(found.get());

                        return Mono.empty();
                    });
        }

        return Flux.empty();
    }
}
