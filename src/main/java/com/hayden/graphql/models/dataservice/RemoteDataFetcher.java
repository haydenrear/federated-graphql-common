package com.hayden.graphql.models.dataservice;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public interface RemoteDataFetcher<T> extends DataFetcher<T> {
}