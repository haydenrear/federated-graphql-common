package com.hayden.graphql.federated.transport.federated_transport;

import com.hayden.graphql.federated.transport.fetcher_transport.FetcherGraphQlTransport;
import com.hayden.graphql.federated.transport.source.FederatedDynamicGraphQlSource;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlClientInterceptor;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;


@SpringBootTest(classes = {
        FederatedDynamicGraphQlSource.class, FederatedGraphQlTransport.class,
        FederatedItemGraphQlTransport.FetcherGraphQlTransportDelegate.class,
        ApplicationEventPublisher.class, FetcherGraphQlTransport.class
})
@ExtendWith(SpringExtension.class)
@ImportAutoConfiguration(DgsAutoConfiguration.class)
class FederatedGraphQlTransportTest {

    @Autowired
    FederatedGraphQlTransport transport;
    @Autowired
    FederatedItemGraphQlTransport.FetcherGraphQlTransportDelegate fetcherGraphQlTransportDelegate;
    @Autowired
    FetcherGraphQlTransport fetcherGraphQlTransport;

    @TestConfiguration
    static class FederatedGraphQlTransportTestConfig {
    }


    @Test
    void next() {
        System.out.println();
//        transport.register()
    }

    @Test
    void execute() {
        var inteceptors = List.of(
                createInterceptor("one"),
                createInterceptor("two"),
                createInterceptor("three")
        );

        GraphQlClientInterceptor reduced = inteceptors.stream().reduce(GraphQlClientInterceptor::andThen).get();
        reduced.intercept(null, null);
    }

    private static GraphQlClientInterceptor createInterceptor(String hello) {
        return new GraphQlClientInterceptor() {
            @Override
            public reactor.core.publisher.Mono<ClientGraphQlResponse> intercept(ClientGraphQlRequest request, Chain chain) {
                System.out.println(hello);
                return GraphQlClientInterceptor.super.intercept(request, chain);
            }
        };
    }

    @Test
    void executeSubscription() {
    }
}