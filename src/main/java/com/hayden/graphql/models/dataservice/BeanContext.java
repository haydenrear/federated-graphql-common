package com.hayden.graphql.models.dataservice;

public interface BeanContext {

    <T> T get(Class<T> clzz, Object... args);

}
