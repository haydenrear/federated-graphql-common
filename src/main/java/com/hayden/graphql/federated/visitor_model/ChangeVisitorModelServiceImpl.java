package com.hayden.graphql.federated.visitor_model;

import com.hayden.graphql.models.visitor.model.VisitorModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Service
public class ChangeVisitorModelServiceImpl implements ChangeVisitorModelService {

    private final List<VisitorModel> visitorModels;

    private final List<ChangeVisitorModel> changeVisitorModels = new ArrayList<>();
    private final AtomicBoolean stale = new AtomicBoolean(false);

    private VisitorModelsContext visitorModelsCtx;

    @PostConstruct
    public void setVisitorModelsCtx() {
        visitorModelsCtx = new VisitorModelsContext(visitorModels);
    }

    @Override
    public VisitorModelsContext get(ChangeVisitorModel.VisitorDelegatesContext delegatesContext) {
        return doUpdate(delegatesContext);
    }

    @Override
    public synchronized void register(ChangeVisitorModel changeVisitorModel) {
        changeVisitorModels.add(changeVisitorModel);
        stale.set(true);
    }

    private synchronized VisitorModelsContext doUpdate(ChangeVisitorModel.VisitorDelegatesContext delegatesContext) {
        if (stale.getAndSet(false)) {
            List<ChangeVisitorModel> toRemove = new ArrayList<>();
            Collections.sort(changeVisitorModels);
            for (ChangeVisitorModel changeVisitorModel : changeVisitorModels) {
                visitorModelsCtx = changeVisitorModel.commandIf(visitorModelsCtx, delegatesContext);

                if (changeVisitorModel.doRemove())
                    toRemove.add(changeVisitorModel);
            }

            changeVisitorModels.removeAll(toRemove);
        }

        return visitorModelsCtx;
    }
}
