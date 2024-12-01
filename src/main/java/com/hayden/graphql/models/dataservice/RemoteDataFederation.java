package com.hayden.graphql.models.dataservice;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import com.hayden.utilitymodule.result.Result;
import graphql.schema.DataFetchingEnvironment;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.graphql.client.ClientGraphQlResponse;
import reactor.core.publisher.Mono;

import java.util.List;


/**
 * Is implemented by the data services and then sent to gateway to compile there is a simple query to call by the data service.
 * @param <ResponseT>
 */
public abstract class RemoteDataFederation<T> extends RemoteDataFetcherImpl<T>{

    @Override
    public <U> Result<T, RemoteDataFetcherError> from(List<U> fromValue) {
        throw new RuntimeException("Implement ClientGraphQlResponse and copy the data...");
    }

    @Override
    public void wire(AutowireCapableBeanFactory beanFactory) {
        super.wire(beanFactory);
    }

    Publisher<T> get(FederatedRequestData environment,
                     FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs federatedGraphQlClient) {
        var executed = this.execute(environment);
        if (executed.isOk())
            return Mono.just(executed.get());

        return Mono.empty();
    }

}
