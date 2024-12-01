package com.hayden.graphql.federated.visitor_model;

import com.hayden.graphql.models.visitor.model.VisitorModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VisitorModelService {

    private final ChangeVisitorModelService visitorModelService;

    public ChangeVisitorModelService.VisitorModelsContext models(ChangeVisitorModel.VisitorDelegatesContext delegatesContext) {
        return Optional.ofNullable(visitorModelService)
                .map(modelService -> modelService.get(delegatesContext))
                .orElseGet(ChangeVisitorModelService.VisitorModelsContext::new);
    }

}
