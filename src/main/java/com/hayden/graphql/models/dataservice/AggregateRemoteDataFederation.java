package com.hayden.graphql.models.dataservice;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import org.reactivestreams.Publisher;
import org.springframework.graphql.client.ClientGraphQlResponse;
import reactor.core.publisher.Flux;

import java.util.Collection;

public abstract class AggregateRemoteDataFederation extends RemoteDataFederation<ClientGraphQlResponse> {

    abstract Collection<RemoteDataFederation<?>> dataFederation();

    /**
     * Must do logging, print...
     *
     * @param fetched
     * @return
     */
    abstract Publisher<ClientGraphQlResponse> aggregate(Publisher<?> fetched);

    Flux<?> fetch(FederatedRequestData environment,
                                      FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs federatedGraphQlClient) {
        return Flux.fromIterable(this.dataFederation())
                .flatMap(r -> {
                    try {
                        return Flux.from(r.get(environment, federatedGraphQlClient));
                    } catch (Exception e) {
                        return Flux.empty();
                    }
                });
    }


    Publisher<ClientGraphQlResponse> get(FederatedRequestData environment,
                                         FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs federatedGraphQlClient) {
        return aggregate(fetch(environment, federatedGraphQlClient));
    }
}
