package com.hayden.graphql.models.visitor.model;

public interface Digest {

    record MessageDigestBytes(byte[] digest) {}

    MessageDigestBytes digest();

}
