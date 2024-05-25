package com.hayden.graphql.models.federated.request;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.hayden.graphql.federated.client.FederatedGraphQlClientBuilderHolder;
import lombok.Getter;
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
    private final FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs client;
    @Getter
    private final Object requestBody;


    public ClientFederatedRequestItem(
            String document,
            @Nullable String operationName,
            Map<String, Object> variables,
            Map<String, Object> extensions,
            Map<String, Object> attributes,
            FederatedRequestData requestData,
            FederatedGraphQlClientBuilderHolder.FederatedGraphQlClient.FederatedGraphQlRequestArgs client
    ) {
        super(document, operationName, variables, extensions);
        this.requestBody = document;
        this.requestData = requestData;
        this.client = client;
        this.attributes.putAll(attributes);
    }



    @Override
    public @NotNull Map<String, Object> getAttributes() {
        return this.attributes;
    }

}
