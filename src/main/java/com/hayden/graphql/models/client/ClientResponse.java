package com.hayden.graphql.models.client;

import com.hayden.utilitymodule.result.Result;

public record ClientResponse(ClientResponseItem[] items) {

    public record ClientResponseItem(Result<Object, Result.Error> results) {
    }

}
