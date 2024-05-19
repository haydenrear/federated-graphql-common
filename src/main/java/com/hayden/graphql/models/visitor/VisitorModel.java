package com.hayden.graphql.models.visitor;

// TODO: add invalidateCurrent
public interface VisitorModel {

    default String version() {
        return "0.0.1";
    }

}
