package com.hayden.graphql.models.federated.request;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FederatedRequestDataItemTest {

    @Test
    public void testBuild() {
        FederatedRequestDataItem build = FederatedRequestDataItem.builder()
                .path("/")
                .build();
    }

}