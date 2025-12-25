package com.hayden.graphql.federated.config;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import com.netflix.graphql.dgs.GraphQlClientProps;
import com.netflix.graphql.dgs.internal.DefaultDgsQueryExecutor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.DgsGraphQlClient;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpSyncGraphQlClient;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(GraphQlClientProps.class)
public class DgsQueryExecutorConfig {

    @Bean
    public DgsGraphQlClient dgsGraphQlClient(GraphQlClientProps graphQlClientProps) {
        return DgsGraphQlClient.create(
                graphQlClient(graphQlClientProps));
    }

    @Bean
    public GraphQlClient graphQlClient(GraphQlClientProps graphQlClientProps) {
        var template = new RestTemplateBuilder();
        template = template.connectTimeout(Duration.ofSeconds(1000));
        template = template.readTimeout(Duration.ofSeconds(1000));
        return HttpSyncGraphQlClient.builder(RestClient.create(template.build()).mutate().baseUrl(graphQlClientProps.getUrl()).build())
                .blockingTimeout(Duration.ofSeconds(1000))
                .url(graphQlClientProps.getUrl())
                .build();
    }

}
