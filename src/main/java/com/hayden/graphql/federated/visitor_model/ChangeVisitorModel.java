package com.hayden.graphql.federated.visitor_model;


import org.jetbrains.annotations.NotNull;

public interface ChangeVisitorModel<T extends ChangeVisitorModel.VisitorDelegatesContext> extends Comparable<ChangeVisitorModel<T>> {

    interface VisitorDelegatesContext {}

    /**
     * If the schema has been removed upon following, for instance, then it will not be in the context now,
     * so then we can remove it.
     * @return
     */
    boolean doRemove();

    /**
     * commandIf should only perform the action one time or be an idempotent action. It is the duty of the
     * implementer to only perform the change once.
     * @param model
     * @return supposedly a new context for guarantees about parallelism
     */
    ChangeVisitorModelServiceImpl.VisitorModelsContext commandIf(ChangeVisitorModelServiceImpl.VisitorModelsContext model,
                                                                 T visitor);


    default int compareTo(@NotNull ChangeVisitorModel<T> o) {
        return 0;
    }

}
