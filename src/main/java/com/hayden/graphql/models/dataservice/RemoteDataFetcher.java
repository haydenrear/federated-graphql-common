package com.hayden.graphql.models.dataservice;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;


/**
 * Is implemented by the data services and then sent to gateway to compile there is a simple query to call by the data service.
 * @param <ResponseT>
 */
public interface RemoteDataFetcher<ResponseT>  {

    ResponseT get(DataServiceRequestExecutor.FederatedRequestData environment,
                  FederatedGraphQlClientBuilder.FederatedGraphQlClient federatedGraphQlClient) throws Exception;

}
