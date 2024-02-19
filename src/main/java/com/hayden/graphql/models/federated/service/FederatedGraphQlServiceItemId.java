package com.hayden.graphql.models.federated.service;

import org.springframework.util.MimeType;

public record FederatedGraphQlServiceItemId(String host, MimeType mimeType, String uniqueFetchId) {
}
