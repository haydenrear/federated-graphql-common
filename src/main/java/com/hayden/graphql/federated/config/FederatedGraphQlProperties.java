package com.hayden.graphql.federated.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "federated")
@Component
@Data
public class FederatedGraphQlProperties {

    long timeoutMillis;

}
