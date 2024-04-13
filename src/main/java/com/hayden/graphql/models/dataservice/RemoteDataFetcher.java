package com.hayden.graphql.models.dataservice;

import com.hayden.graphql.models.federated.request.FederatedRequestData;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

/**
 * The fetcher that the data services provide to the Gateway in addition to the schema. This allows
 * for the GraphQL Federation through a DataFetcher.
 * @param <T>
 */
public interface RemoteDataFetcher<T> extends DataFetcher<T>, ApplicationContextAware {

    BeanContext ctx(DataFetchingEnvironment env);

    T execute(DataFetchingEnvironment env);

    FederatedRequestData toRequestData(DataFetchingEnvironment env);

    <U> T from(List<U> fromValue);

    /**
     * Wire the context so that beans are available.
     * @param beanFactory
     */
    default void wire(AutowireCapableBeanFactory beanFactory) {
        beanFactory.autowireBean(this);
    }

}