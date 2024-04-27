package com.hayden.graphql.federated;

import com.hayden.graphql.federated.transport.source.FederatedDynamicGraphQlSource;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.graphql.execution.DefaultExecutionGraphQlService;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Component;

/**
 * Gets injected as graphQlSource bean into Spring.
 */
@Component("graphQlSource")
public class FederatedGraphQlSourceProvider implements GraphQlSource {


    @Autowired @Lazy
    private FederatedDynamicGraphQlSource federatedDynamicGraphQlSource;

    private volatile DefaultExecutionGraphQlService defaultExecutionService;
    private volatile boolean doReload;


    public FederatedDynamicGraphQlSource federatedDynamicGraphQlSource() {
        return federatedDynamicGraphQlSource;
    }

    public void setReload() {
        doReload = true;
    }

    public void reload() {
        this.federatedDynamicGraphQlSource.reload(doReload);
        this.doReload = false;
    }

    public @NotNull GraphQL graphQl() {
        reload();
        return this.federatedDynamicGraphQlSource().graphQl();
    }

    public @NotNull GraphQLSchema schema() {
        reload();
        return this.federatedDynamicGraphQlSource().schema();
    }


    public DefaultExecutionGraphQlService executionGraphQlService() {
        if (this.defaultExecutionService == null) {
            synchronized (this) {
                if (this.defaultExecutionService == null)
                    this.defaultExecutionService = new DefaultExecutionGraphQlService(this);
            }
        }

        return this.defaultExecutionService;
    }

}
