package com.hayden.graphql.models.visitor.datafetcher;

import com.hayden.graphql.models.visitor.VisitorModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record DeleteSchemaApiVisitorModel(String toDelete) implements VisitorModel {
}
