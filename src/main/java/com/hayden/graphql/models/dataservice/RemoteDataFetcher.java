package com.hayden.graphql.models.dataservice;

import com.google.common.collect.Sets;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import com.hayden.utilitymodule.result.error.AggregateError;
import com.hayden.utilitymodule.result.error.ErrorCollect;
import com.hayden.utilitymodule.result.Result;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Set;

/**
 * The fetcher that the data services provide to the Gateway in addition to the schema. This allows
 * for the GraphQL Federation through a DataFetcher.
 * @param <T>
 */
public interface RemoteDataFetcher<T> extends DataFetcher<T>, ApplicationContextAware {

    BeanContext ctx(DataFetchingEnvironment env);

    Result<T, RemoteDataFetcherError> execute(DataFetchingEnvironment env);

    FederatedRequestData toRequestData(DataFetchingEnvironment env);

    <U> Result<T, RemoteDataFetcherError> from(List<U> fromValue);

    /**
     * Wire the context so that beans are available.
     * @param beanFactory
     */
    default void wire(AutowireCapableBeanFactory beanFactory) {
        beanFactory.autowireBean(this);
    }


    record RemoteDataFetcherError(Set<ErrorCollect> errors) implements AggregateError {
        public RemoteDataFetcherError(Throwable throwable) {
            this(Sets.newHashSet(ErrorCollect.fromE(throwable)));
        }
        public RemoteDataFetcherError(String throwable) {
            this(Sets.newHashSet(ErrorCollect.fromMessage(throwable)));
        }
        public RemoteDataFetcherError(ErrorCollect throwable) {
            this(Sets.newHashSet(throwable));
        }
    }

}