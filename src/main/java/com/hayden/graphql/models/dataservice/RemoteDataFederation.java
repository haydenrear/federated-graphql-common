package com.hayden.graphql.models.dataservice;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.graphql.client.ClientGraphQlResponse;


/**
 * Is implemented by the data services and then sent to gateway to compile there is a simple query to call by the data service.
 * @param <ResponseT>
 */
public interface RemoteDataFederation {

    Publisher<ClientGraphQlResponse> get(FederatedRequestData environment,
                                         FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs federatedGraphQlClient);

    default void wire(AutowireCapableBeanFactory beanFactory) {
        beanFactory.autowireBean(this);
    }

}
