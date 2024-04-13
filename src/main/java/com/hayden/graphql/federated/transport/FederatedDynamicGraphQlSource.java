package com.hayden.graphql.federated.transport;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.internal.DefaultDgsQueryExecutor;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FederatedDynamicGraphQlSource implements GraphQlSource {

    private GraphQL graphQl;
    private GraphQLSchema graphQLSchema;


    private final DgsQueryExecutor queryExecutor;


    volatile boolean doReload;


    public void setReload() {
        doReload = true;
    }


    private void reload() {
        if (this.graphQLSchema == null) {
            this.graphQLSchema = ((DefaultDgsQueryExecutor) queryExecutor).getSchema().get();
            this.graphQl = GraphQL.newGraphQL(this.graphQLSchema).build();
        }
        if (doReload) {
            synchronized (this) {
                if (doReload) {
                    this.graphQLSchema = ((DefaultDgsQueryExecutor)queryExecutor).getSchema().get();
                    this.graphQl = GraphQL.newGraphQL(this.graphQLSchema).build();
                    doReload = false;
                }
            }
        }
    }

    @Override
    public @NotNull GraphQL graphQl() {
        reload();
        return graphQl;
    }

    @Override
    public @NotNull GraphQLSchema schema() {
        reload();
        return graphQLSchema;
    }
}
