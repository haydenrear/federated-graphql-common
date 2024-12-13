package com.hayden.graphql.models.dataservice;

import com.google.common.collect.Sets;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import com.hayden.utilitymodule.result.agg.AggregateError;
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

    Result<T, RemoteDataFetcherError> execute(FederatedRequestData env);

    FederatedRequestData toRequestData(DataFetchingEnvironment env);

    <U> Result<T, RemoteDataFetcherError> from(List<U> fromValue);

    record RequestTemplate(String document, GraphQlRequestType graphQlRequestType) {
        public enum GraphQlRequestType {
            QUERY, MUTATION, SUBSCRIPTION
        }

        public static RequestTemplate mutationOf(String document) {
            return new RequestTemplate(document, GraphQlRequestType.MUTATION);
        }

        public static RequestTemplate queryOf(String document) {
            return new RequestTemplate(document, GraphQlRequestType.QUERY);
        }

        public static RequestTemplate subscriptionOf(String document) {
            return new RequestTemplate(document, GraphQlRequestType.SUBSCRIPTION);
        }
    }

    List<RequestTemplate> requestTemplates();

    /**
     * Wire the context so that beans are available.
     * @param beanFactory
     */
    default void wire(AutowireCapableBeanFactory beanFactory) {
        beanFactory.autowireBean(this);
    }


    record RemoteDataFetcherError(Set<ErrorCollect> errors) implements AggregateError.StdAggregateError {
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