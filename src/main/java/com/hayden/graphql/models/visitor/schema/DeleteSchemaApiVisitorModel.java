package com.hayden.graphql.models.visitor.schema;

import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceFetcherItemId;
import com.hayden.graphql.models.visitor.model.VisitorModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record DeleteSchemaApiVisitorModel(String toDelete,
                                          FederatedGraphQlServiceFetcherItemId fetcherId,
                                          boolean invalidateCurrent,
                                          FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceInstanceId id) implements VisitorModel {
}
