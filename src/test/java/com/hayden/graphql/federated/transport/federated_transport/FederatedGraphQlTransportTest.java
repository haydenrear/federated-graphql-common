package com.hayden.graphql.federated.transport.federated_transport;

import com.hayden.graphql.federated.FederatedGraphQlSourceProvider;
import com.hayden.graphql.federated.config.DgsQueryExecutorConfig;
import com.hayden.graphql.federated.config.FederatedGraphQlProperties;
import com.hayden.graphql.federated.transport.fetcher_transport.FetcherGraphQlTransport;
import com.hayden.graphql.federated.transport.source.FederatedDynamicGraphQlSource;
import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import com.hayden.graphql.models.federated.request.FederatedGraphQlRequest;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceFetcherItemId;
import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import com.netflix.graphql.dgs.internal.DefaultDgsQueryExecutor;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.MimeType;
import reactor.test.StepVerifier;

import java.util.Map;


@SpringBootTest(classes = {
        FederatedDynamicGraphQlSource.class, FederatedGraphQlTransport.class,
        FederatedItemGraphQlTransport.CallDataFetchersFederatedGraphQlTransport.class,
        ApplicationEventPublisher.class, FetcherGraphQlTransport.class,
        FederatedGraphQlSourceProvider.class, DgsQueryExecutor.class,
        DefaultDgsQueryExecutor.class, DgsQueryExecutorConfig.class
})
@EnableConfigurationProperties(FederatedGraphQlProperties.class)
@ExtendWith(SpringExtension.class)
@Import(DgsQueryExecutorConfig.class)
class FederatedGraphQlTransportTest {

    @Autowired
    FederatedGraphQlTransport transport;
    @Autowired
    FederatedItemGraphQlTransport.CallDataFetchersFederatedGraphQlTransport callDataFetchersFederatedGraphQlTransport;
    @Autowired
    FetcherGraphQlTransport fetcherGraphQlTransport;

    @TestConfiguration
    static class FederatedGraphQlTransportTestConfig {
    }


    @Test
    void next() {
        StepVerifier.create(transport.next(new FederatedGraphQlRequest(getRequest())))
                .assertNext(c -> {

                })
                .expectComplete()
                .verify();


    }

    private static @NotNull Map<FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceFetcherId, FederatedGraphQlRequest.FederatedClientGraphQlRequestItem> getRequest() {
        FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceId serviceId = new FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceId("test");
        var serviceFetcherId = new FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceFetcherId(MimeType.valueOf("text/html"), "test", serviceId);
        FederatedGraphQlServiceFetcherItemId.FederatedGraphQlHost host = new FederatedGraphQlServiceFetcherItemId.FederatedGraphQlHost("localhost");
        FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceInstanceId serviceInstanceId = new FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceInstanceId(serviceId, host);
        var service = new FederatedGraphQlServiceFetcherItemId(serviceFetcherId, serviceInstanceId);
        return Map.of(
                serviceFetcherId,
                new FederatedGraphQlRequest.FederatedClientGraphQlRequestItem(serviceFetcherId, createRequestItem())
        );
    }

    private static @NotNull ClientFederatedRequestItem createRequestItem() {
//        return new ClientFederatedRequestItem();
        return null;
    }

    @Test
    void executeSubscription() {
    }
}