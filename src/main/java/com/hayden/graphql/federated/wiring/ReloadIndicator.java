package com.hayden.graphql.federated.wiring;

public interface ReloadIndicator {
    boolean doReload();

    void setReload(boolean toSet);

    default void setReload() {
        setReload(true);
    }
}
