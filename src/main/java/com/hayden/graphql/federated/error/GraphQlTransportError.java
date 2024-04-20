package com.hayden.graphql.federated.error;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.ArrayList;
import java.util.List;

public record GraphQlTransportError(String message, List<SourceLocation> sourceLocations, ErrorClassification errorType) implements GraphQLError {

    public GraphQlTransportError(String message) {
        this(message, new ArrayList<>(), ErrorClassification.errorClassification("GraphQlTransportError"));
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return sourceLocations;
    }

    @Override
    public ErrorClassification getErrorType() {
        return errorType;
    }
}
