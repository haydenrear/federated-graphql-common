package com.hayden.graphql.federated.transport.source;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.internal.DefaultDgsQueryExecutor;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.graphql.client.DgsGraphQlClient;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Component;

@Component("federatedDynamicGraphQlSource")
@RequiredArgsConstructor
@Slf4j
public class FederatedDynamicGraphQlSource implements GraphQlSource {

    private GraphQL graphQl;
    private GraphQLSchema graphQLSchema;

    private DgsQueryExecutor dgsQueryExecutor;

    private final DgsGraphQlClient dgsGraphQlClient;


    private GraphQlSource graphQlSource;

    @Autowired
    @Qualifier("graphQlSource")
    public void setGraphQlSource(GraphQlSource graphQlSource) {
        this.graphQlSource = graphQlSource;
    }

    @PostConstruct
    public void setDgsQueryExecutor() {
        this.dgsQueryExecutor = new DefaultDgsQueryExecutor(graphQlSource, dgsGraphQlClient);
    }

    public void reload(boolean doReload) {
        if (this.graphQLSchema == null) {
            this.graphQLSchema = ((DefaultDgsQueryExecutor) dgsQueryExecutor).getSchema().get();
            this.graphQl = GraphQL.newGraphQL(this.graphQLSchema).build();
        }
        if (doReload) {
            this.graphQLSchema = ((DefaultDgsQueryExecutor) dgsQueryExecutor).getSchema().get();
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
