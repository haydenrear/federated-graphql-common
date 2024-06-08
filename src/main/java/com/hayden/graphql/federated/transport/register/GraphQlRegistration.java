package com.hayden.graphql.federated.transport.register;

import com.hayden.graphql.federated.transport.federated_transport.FederatedItemGraphQlTransport;
import com.hayden.graphql.models.federated.request.FederatedGraphQlRequest;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceFetcherItemId;
import com.hayden.graphql.models.visitor.model.DataTemplate;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.client.GraphQlTransport;
import org.springframework.util.MimeType;

public interface GraphQlRegistration {

    class GraphQlRegistrations {
        public static FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceFetcherId FEDERATED_SERVICE_FETCHER_ID;
        public static FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceId FEDERATED_SERVICE_ID;
        public static FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceInstanceId FEDERATED_SERVICE_INSTANCE_ID;
        public static FederatedGraphQlServiceFetcherItemId SERVICE_FETCHER_ITEM_ID;
        public static FederatedGraphQlServiceFetcherItemId.FederatedGraphQlHost FEDERATED_HOST;


        static {
            FEDERATED_HOST = new FederatedGraphQlServiceFetcherItemId.FederatedGraphQlHost("gateway");
            FEDERATED_SERVICE_ID = new FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceId("FETCHERS");
            FEDERATED_SERVICE_INSTANCE_ID = new FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceInstanceId(FEDERATED_SERVICE_ID, FEDERATED_HOST);
            FEDERATED_SERVICE_FETCHER_ID = new FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceFetcherId(new MimeType("", ""), "FETCHERS", FEDERATED_SERVICE_ID);
            SERVICE_FETCHER_ITEM_ID =  new FederatedGraphQlServiceFetcherItemId(FEDERATED_SERVICE_FETCHER_ID, FEDERATED_SERVICE_INSTANCE_ID);
        }

    }


    FederatedGraphQlServiceFetcherItemId id();

    /**
     * Access single graphql service, through HttpGraphQlTransport, for instance.
     * @param graphQlTransport
     * @param id
     * @param dataTemplate
     */
    record GraphQlTransportFederatedGraphQlRegistration(
            GraphQlTransport graphQlTransport,
            FederatedGraphQlServiceFetcherItemId id,
            DataTemplate dataTemplate) implements GraphQlRegistration {
        public GraphQlTransportFederatedGraphQlRegistration(
                GraphQlTransport graphQlTransport,
                FederatedGraphQlServiceFetcherItemId id
        ) {
            this (graphQlTransport, id, null);
        }
    }

    /**
     * Access the DGS context, to get the data fetcher that will get the above registration eventually.
     * @param graphQlTransport
     * @param id
     */
    record FederatedItemFederatedGraphQlTransportRegistration(FederatedItemGraphQlTransport<? extends ClientGraphQlRequest> graphQlTransport)
            implements GraphQlRegistration {

        @Override
        public FederatedGraphQlServiceFetcherItemId id() {
            return GraphQlRegistrations.SERVICE_FETCHER_ITEM_ID;
        }
    }
}
