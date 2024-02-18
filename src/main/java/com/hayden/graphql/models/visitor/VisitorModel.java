package com.hayden.graphql.models.visitor;

public interface VisitorModel {

    default String version() {
        return "0.0.1";
    }

}
