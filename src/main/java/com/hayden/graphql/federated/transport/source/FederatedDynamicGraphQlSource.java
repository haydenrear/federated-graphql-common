package com.hayden.graphql.federated.transport.source;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.internal.DefaultDgsQueryExecutor;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FederatedDynamicGraphQlSource implements GraphQlSource {

    private GraphQL graphQl;
    private GraphQLSchema graphQLSchema;

    @Autowired
    private DgsQueryExecutor queryExecutor;


    public void reload(boolean doReload) {
        if (this.graphQLSchema == null) {
            this.graphQLSchema = ((DefaultDgsQueryExecutor)queryExecutor).getSchema().get();
            this.graphQl = GraphQL.newGraphQL(this.graphQLSchema).build();
        }
        if (doReload) {
            this.graphQLSchema = ((DefaultDgsQueryExecutor)queryExecutor).getSchema().get();
            this.graphQl = GraphQL.newGraphQL(this.graphQLSchema).build();
        }
    }

    @Override
    public @NotNull GraphQL graphQl() {
        return graphQl;
    }

    @Override
    public @NotNull GraphQLSchema schema() {
        return graphQLSchema;
    }
}
