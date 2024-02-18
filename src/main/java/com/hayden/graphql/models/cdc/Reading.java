package com.hayden.graphql.models.cdc;


import org.springframework.boot.autoconfigure.kafka.KafkaProperties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Reading {

    String transactionParameter = "";
    String isolationLevel = "";
    String table = "";


}
