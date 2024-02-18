package com.hayden.graphql.models.dataservice;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.GraphQlClientInterceptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class GraphQlFederatedInterceptor implements GraphQlClientInterceptor{

    public Mono<ClientGraphQlResponse> intercept(ClientGraphQlRequest request, Chain chain) {
        if (chain instanceof FederatedChain federatedChain && request instanceof FederatedGraphQlRequest federatedGraphQlRequest) {
            throw new NotImplementedException("Federated cannot intercept with single response");
        }
        return GraphQlClientInterceptor.super.intercept(request, chain);
    }

    public Flux<ClientGraphQlResponse> interceptSubscription(ClientGraphQlRequest request, SubscriptionChain chain) {
        if (chain instanceof FederatedSubscriptionChain federatedChain && request instanceof FederatedGraphQlRequest federatedGraphQlRequest) {
            return Flux.fromIterable(federatedGraphQlRequest.delegators().keySet())
                    .flatMap(r -> federatedChain.next(federatedGraphQlRequest, r));
        }
        return GraphQlClientInterceptor.super.interceptSubscription(request, chain);
    }



    /**
     * Contract for delegation of single response requests to the rest of the chain.
     */
    interface FederatedChain extends Chain {

        /**
         * Delegate to the rest of the chain to perform the request.
         * @param request the request to perform
         * @return {@code Mono} with the response
         * @see GraphQlClient.RequestSpec#execute()
         */
        Mono<ClientGraphQlResponse> next(ClientGraphQlRequest request);

        default Mono<ClientGraphQlResponse> next(FederatedGraphQlRequest request, FederatedGraphQlService service) {
            return Mono.justOrEmpty(request.service(service)).flatMap(this::next);
        }

    }


    /**
     * Contract for delegation of subscription requests to the rest of the chain.
     */
    interface FederatedSubscriptionChain extends SubscriptionChain {

        /**
         * Delegate to the rest of the chain to perform the request.
         * @param request the request to perform
         * @return {@code Flux} with responses
         * @see GraphQlClient.RequestSpec#executeSubscription()
         */
        Flux<ClientGraphQlResponse> next(ClientGraphQlRequest request);

        default Flux<ClientGraphQlResponse> next(FederatedGraphQlRequest request, FederatedGraphQlService service) {
            return Mono.justOrEmpty(request.service(service))
                    .flatMapMany(this::next);
        }

    }

}
