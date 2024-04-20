package com.hayden.graphql.models.dataservice;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import org.reactivestreams.Publisher;
import org.springframework.graphql.client.ClientGraphQlResponse;
import reactor.core.publisher.Flux;

import java.util.Collection;

public interface AggregateRemoteDataFederation {

    Collection<RemoteDataFederation> dataFetchers();

    /**
     * Must do logging, print...
     *
     * @param fetched
     * @return
     */
    Publisher<ClientGraphQlResponse> aggregate(Publisher<ClientGraphQlResponse> fetched);

    default Flux<ClientGraphQlResponse> fetch(FederatedRequestData environment,
                                              FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs federatedGraphQlClient) {
        return Flux.fromIterable(this.dataFetchers())
                .flatMap(r -> {
                    try {
                        return Flux.from(r.get(environment, federatedGraphQlClient));
                    } catch (Exception e) {
                        return Flux.empty();
                    }
                });
    }


    default Publisher<ClientGraphQlResponse> get(FederatedRequestData environment,
                                                 FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs federatedGraphQlClient) {
        return aggregate(fetch(environment, federatedGraphQlClient));
    }
}
