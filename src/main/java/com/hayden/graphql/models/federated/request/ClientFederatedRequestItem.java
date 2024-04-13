package com.hayden.graphql.models.federated.request;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.graphql.client.ClientGraphQlRequest;
import org.springframework.graphql.support.DefaultGraphQlRequest;
import org.springframework.lang.Nullable;

/**
 * Default implementation of {@link ClientGraphQlRequest}.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
public class ClientFederatedRequestItem extends DefaultGraphQlRequest implements ClientGraphQlRequest {

    private final Map<String, Object> attributes = new ConcurrentHashMap<>();
    @Getter
    private final FederatedRequestData requestData;
    @Getter @NotNull
    private final FederatedGraphQlClientBuilder.FederatedGraphQlClient.FederatedGraphQlRequestArgs client;
    @Getter
    private final Object requestBody;


    public ClientFederatedRequestItem(
            Object document,
            @Nullable String operationName,
            Map<String, Object> variables,
            Map<String, Object> extensions,
            Map<String, Object> attributes,
            FederatedRequestData requestData,
            FederatedGraphQlClientBuilder.FederatedGraphQlClient.FederatedGraphQlRequestArgs client
    ) {
        super(String.valueOf(document), operationName, variables, extensions);
        this.requestBody = document;
        this.requestData = requestData;
        this.client = client;
        this.attributes.putAll(attributes);
    }



    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

}
