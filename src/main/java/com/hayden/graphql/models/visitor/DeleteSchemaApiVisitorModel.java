package com.hayden.graphql.models.visitor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record DeleteSchemaApiVisitorModel(String toDelete) implements VisitorModel {
}
