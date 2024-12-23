package com.hayden.graphql.models.dataservice;

import com.hayden.graphql.federated.FederatedExecutionGraphQlService;
import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import com.hayden.graphql.federated.execution.DataServiceRequestExecutor;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import com.hayden.utilitymodule.result.Result;
import graphql.schema.DataFetchingEnvironment;
import jakarta.annotation.Nonnull;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public abstract class RemoteDataFetcherImpl<T> implements RemoteDataFetcher<T>, ApplicationContextAware {

    @Delegate
    private ApplicationContext applicationContext;

    @Autowired
    private FederatedExecutionGraphQlService executionService;


    /**
     * Ability to retrieve any bean from the context.
     * @param env
     * @return
     */
    @Override
    public BeanContext ctx(DataFetchingEnvironment env) {
        return new BeanContext() {
            @Override
            public <U> U get(Class<U> clzz, Object... args) {
                return applicationContext.getBean(clzz, args);
            }
        };
    }

    @Override
    public Result<T, RemoteDataFetcherError> execute(DataFetchingEnvironment env) {
        return this.execute(toRequestData(env));
    }

    @Override
    public Result<T, RemoteDataFetcherError> execute(FederatedRequestData requestData) {
        return this.toResultData(
                Flux.using(
                                () -> executionService.federatedClient(),
                                federatedClient -> federatedClient.request(requestData),
                                FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient::close
                        )
                        .log()
                        .collectList()
        );
    }

    @NotNull
    private Result<T, RemoteDataFetcherError> toResultData(Mono<List<DataServiceRequestExecutor.FederatedGraphQlResponse>> t) {
        try {
            return t.map(this::convert)
                    .log()
                    .toFuture()
                    .get();
        } catch (InterruptedException |
                 ExecutionException e) {
            log.error("Error when attempting to get response: {}.", e.getMessage());
            return Result.err(new RemoteDataFetcherError(e));
        }
    }

    private Result<T, RemoteDataFetcherError> convert(List<DataServiceRequestExecutor.FederatedGraphQlResponse> l) {
        List<?> value = l.stream()
                .flatMap(result -> {
                    try {
                        return Stream.of(Result.ok(result.toResult().<T>getData()));
                    } catch (
                            ClassCastException c) {
                        log.error("Error when converting {} with error {}.", result.toResult(), c.getMessage());
                        return Stream.of(Result.err(new RemoteDataFetcherError(c)));
                    }
                })
                .collect(Collectors.toList());
        return this.from(value);
    }

    @Override
    @Autowired
    public void setApplicationContext(@Nonnull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
