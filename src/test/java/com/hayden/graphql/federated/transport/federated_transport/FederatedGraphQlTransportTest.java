package com.hayden.graphql.federated.transport.federated_transport;

import com.hayden.graphql.federated.transport.fetcher_transport.FetcherGraphQlTransport;
import com.hayden.graphql.federated.transport.source.FederatedDynamicGraphQlSource;
import com.hayden.graphql.models.federated.request.ClientFederatedRequestItem;
import com.hayden.graphql.models.federated.request.FederatedGraphQlRequest;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.MimeType;
import reactor.test.StepVerifier;

import java.util.Map;


@SpringBootTest(classes = {
        FederatedDynamicGraphQlSource.class, FederatedGraphQlTransport.class,
        FederatedItemGraphQlTransport.CallDataFetchersFederatedGraphQlTransport.class,
        ApplicationEventPublisher.class, FetcherGraphQlTransport.class
})
@ExtendWith(SpringExtension.class)
@ImportAutoConfiguration(DgsAutoConfiguration.class)
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

    private static @NotNull Map<FederatedGraphQlServiceItemId.FederatedGraphQlServiceId, FederatedGraphQlRequest.FederatedClientGraphQlRequestItem> getRequest() {
        var serviceId = new FederatedGraphQlServiceItemId.FederatedGraphQlServiceId(MimeType.valueOf("text/html"), "test", "test");
        var service = new FederatedGraphQlServiceItemId(serviceId, "test", "localhost");
        return Map.of(
                serviceId,
                new FederatedGraphQlRequest.FederatedClientGraphQlRequestItem(serviceId, createRequestItem())
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