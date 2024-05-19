package com.hayden.graphql.models.visitor.model;

import lombok.experimental.Delegate;

import java.util.List;

public interface VisitorModel extends Invalidatable, Id {

    default String version() {
        return "0.0.1";
    }

    record VisitorResponse(List<VisitorModelResponse> visitorModels, Digest.MessageDigestBytes digest) implements Digest {}
    record VisitorModelResponse(@Delegate VisitorModel model, Digest.MessageDigestBytes digest) implements Digest, Id, Invalidatable {}

}
