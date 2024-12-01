package com.hayden.graphql.federated.visitor_model;

import com.hayden.graphql.models.visitor.model.VisitorModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public interface ChangeVisitorModelService {

    ChangeVisitorModelServiceImpl.VisitorModelsContext get(ChangeVisitorModel.VisitorDelegatesContext delegatesContext);

    void register(ChangeVisitorModel changeVisitorModel);

    record VisitorModelsContext(List<VisitorModel> visitorModels) {

        public VisitorModelsContext() {
            this(new ArrayList<>());
        }

        public List<VisitorModel> visitorModels() {
            return Collections.unmodifiableList(visitorModels);
        }

        public List<VisitorModel> sendVisitorModels() {
            return this.visitorModels();
        }

    }
}
