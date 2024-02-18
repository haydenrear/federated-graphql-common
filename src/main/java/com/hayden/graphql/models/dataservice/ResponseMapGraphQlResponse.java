package com.hayden.graphql.models.dataservice;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import org.springframework.graphql.GraphQlResponse;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.support.AbstractGraphQlResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * {@link GraphQlResponse} that wraps a deserialized the GraphQL response map.
 *
 * @author Rossen Stoyanchev
 * @since 1.0.0
 */
class ResponseMapGraphQlResponse extends AbstractGraphQlResponse {

    private final Map<String, Object> responseMap;

    private final List<ResponseError> errors;


    ResponseMapGraphQlResponse(Map<String, Object> responseMap) {
        Assert.notNull(responseMap, "'responseMap' is required");
        this.responseMap = responseMap;
        this.errors = wrapErrors(responseMap);
    }

    protected ResponseMapGraphQlResponse(GraphQlResponse response) {
        Assert.notNull(response, "'GraphQlResponse' is required");
        this.responseMap = response.toMap();
        this.errors =  response.getErrors();
    }

    @SuppressWarnings("unchecked")
    private static List<ResponseError> wrapErrors(Map<String, Object> map) {
        List<Map<String, Object>> errors = (List<Map<String, Object>>) map.get("errors");
        errors = (errors != null ? errors : Collections.emptyList());
        return errors.stream().map(MapResponseError::new).collect(Collectors.toList());
    }


    @Override
    public boolean isValid() {
        return (this.responseMap.containsKey("data") && this.responseMap.get("data") != null);
    }

    @Override
    public List<ResponseError> getErrors() {
        return this.errors;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getData() {
        return (T) this.responseMap.get("data");
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Object, Object> getExtensions() {
        return (Map<Object, Object>) this.responseMap.getOrDefault("extensions", Collections.emptyMap());
    }

    @Override
    public Map<String, Object> toMap() {
        return this.responseMap;
    }

    @Override
    public boolean equals(Object other) {
        return (other instanceof ResponseMapGraphQlResponse &&
                this.responseMap.equals(((ResponseMapGraphQlResponse) other).responseMap));
    }

    @Override
    public int hashCode() {
        return this.responseMap.hashCode();
    }

    @Override
    public String toString() {
        return this.responseMap.toString();
    }


    /**
     * {@link GraphQLError} that wraps a deserialized the GraphQL response map.
     */
    private static final class MapResponseError implements ResponseError {

        private final Map<String, Object> errorMap;

        private final List<SourceLocation> locations;

        private final String path;

        MapResponseError(Map<String, Object> errorMap) {
            Assert.notNull(errorMap, "'errorMap' is required");
            this.errorMap = errorMap;
            this.locations = initSourceLocations(errorMap);
            this.path = initPath(errorMap);
        }

        @SuppressWarnings("unchecked")
        private static List<SourceLocation> initSourceLocations(Map<String, Object> errorMap) {
            List<Map<String, Object>> locations = (List<Map<String, Object>>) errorMap.get("locations");
            if (locations == null) {
                return Collections.emptyList();
            }
            return locations.stream()
                    .map(m -> new SourceLocation(getInt(m, "line"), getInt(m, "column"), (String) m.get("sourceName")))
                    .collect(Collectors.toList());
        }

        private static int getInt(Map<String, Object> map, String key) {
            if (map.get(key) instanceof Number number) {
                return number.intValue();
            }
            else {
                throw new IllegalArgumentException(
                        "Expected integer value: " + ObjectUtils.nullSafeClassName(map.get(key)));
            }
        }

        @SuppressWarnings("unchecked")
        private static String initPath(Map<String, Object> errorMap) {
            List<Object> path = (List<Object>) errorMap.get("path");
            if (path == null) {
                return "";
            }
            return path.stream().reduce("",
                    (s, o) -> s + (o instanceof Integer ? "[" + o + "]" : (s.isEmpty() ? o : "." + o)),
                    (s, s2) -> null);
        }


        @Override
        @Nullable
        public String getMessage() {
            return (String) errorMap.get("message");
        }

        @Override
        public List<SourceLocation> getLocations() {
            return this.locations;
        }

        @Override
        public ErrorClassification getErrorType() {
            String classification = (String) getExtensions().getOrDefault("classification", "");
            try {
                return graphql.ErrorType.valueOf(classification);
            }
            catch (IllegalArgumentException ex) {
                return org.springframework.graphql.execution.ErrorType.valueOf(classification);
            }
        }

        @Override
        public String getPath() {
            return this.path;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<Object> getParsedPath() {
            return (List<Object>) this.errorMap.getOrDefault("path", Collections.emptyList());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Map<String, Object> getExtensions() {
            return (Map<String, Object>) this.errorMap.getOrDefault("extensions", Collections.emptyMap());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            ResponseError other = (ResponseError) o;
            return (ObjectUtils.nullSafeEquals(getMessage(), other.getMessage()) &&
                    ObjectUtils.nullSafeEquals(getLocations(), other.getLocations()) &&
                    ObjectUtils.nullSafeEquals(getParsedPath(), other.getParsedPath()) &&
                    getErrorType() == other.getErrorType());
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = 31 * result + ObjectUtils.nullSafeHashCode(getMessage());
            result = 31 * result + ObjectUtils.nullSafeHashCode(getLocations());
            result = 31 * result + ObjectUtils.nullSafeHashCode(getParsedPath());
            result = 31 * result + ObjectUtils.nullSafeHashCode(getErrorType());
            return result;
        }

        @Override
        public String toString() {
            return this.errorMap.toString();
        }

    }

}
