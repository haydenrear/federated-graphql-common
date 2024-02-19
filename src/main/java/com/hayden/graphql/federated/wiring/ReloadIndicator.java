package com.hayden.graphql.federated.wiring;

public interface ReloadIndicator {
    boolean doReload();

    boolean doDidReload();

    void setReload(boolean toSet);

    default void setReload() {
        setReload(true);
    }
}
