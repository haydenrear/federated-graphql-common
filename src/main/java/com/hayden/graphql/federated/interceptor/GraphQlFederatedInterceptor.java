package com.hayden.graphql.federated.interceptor;

import com.hayden.graphql.models.federated.request.FederatedGraphQlRequest;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.GraphQlClientInterceptor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public class GraphQlFederatedInterceptor implements GraphQlClientInterceptor{

    public Mono<ClientGraphQlResponse> intercept(ClientGraphQlRequest request, Chain chain) {
        if (request instanceof FederatedGraphQlRequest) {
            throw new UnsupportedOperationException();
        }

        return GraphQlClientInterceptor.super.intercept(request, chain);
    }

    public Flux<ClientGraphQlResponse> interceptSubscription(ClientGraphQlRequest request, SubscriptionChain chain) {
        if (chain instanceof FederatedSubscriptionChain federatedChain
                && request instanceof FederatedGraphQlRequest federatedGraphQlRequest) {
            return Flux.fromIterable(federatedGraphQlRequest.delegators().keySet())
                    .flatMap(r -> federatedChain.next(federatedGraphQlRequest, r));
        } else if (request instanceof FederatedGraphQlRequest) {
            throw new UnsupportedOperationException();
        }
        return GraphQlClientInterceptor.super.interceptSubscription(request, chain);
    }



    /**
     * Contract for delegation of subscription requests to the rest of the chain.
     */
    public interface FederatedSubscriptionChain extends SubscriptionChain {

        /**
         * Delegate to the rest of the chain to perform the request.
         * @param request the request to perform
         * @return {@code Flux} with responses
         * @see GraphQlClient.RequestSpec#executeSubscription()
         */
        Flux<ClientGraphQlResponse> next(ClientGraphQlRequest request);

        default Flux<ClientGraphQlResponse> next(FederatedGraphQlRequest request,
                                                 FederatedGraphQlServiceItemId service) {
            return Mono.justOrEmpty(request.service(service))
                    .flatMapMany(this::next);
        }
    }

}
