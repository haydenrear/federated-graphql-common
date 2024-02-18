package com.hayden.graphql.models.dataservice;

import com.hayden.utilitymodule.result.Result;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public interface AggregateRemoteDataFetcher<ResponseT> {

    Collection<RemoteDataFetcher<?>> dataFetchers();

    /**
     * Must do logging, print...
     *
     * @param fetched
     * @return
     */
    ResponseT aggregate(Collection<Result<?, Result.Error>> fetched);

    default Collection<Result<?, Result.Error>> fetch(DataServiceRequestExecutor.FederatedRequestData environment,
                                                      FederatedGraphQlClientBuilder.FederatedGraphQlClient federatedGraphQlClient) {
        return this.dataFetchers().stream()
                .map(r -> {
                    try {
                        return Result.fromResult(r.get(environment, federatedGraphQlClient));
                    } catch (Exception e) {
                        return Result.fromError(e.getMessage());
                    }
                })
                .map(r -> (Result<?, Result.Error>) r)
                .collect(Collectors.toCollection(ArrayList::new));
    }


    default ResponseT get(DataServiceRequestExecutor.FederatedRequestData environment,
                          FederatedGraphQlClientBuilder.FederatedGraphQlClient federatedGraphQlClient) throws Exception {
        return aggregate(fetch(environment, federatedGraphQlClient));
    }
}
