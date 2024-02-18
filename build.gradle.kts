plugins {
    id("com.hayden.test-jar")
    id("com.hayden.no-main-class")
}

group = "com.hayden"
version = "0.0.1-SNAPSHOT"

tasks.register("prepareKotlinBuildScriptModel")

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("com.netflix.graphql.dgs:graphql-dgs-spring-boot-starter")
    implementation("com.netflix.graphql.dgs.codegen:graphql-dgs-codegen-core:6.1.4")
    implementation("com.netflix.graphql.dgs:graphql-dgs-mocking:8.2.5")
    implementation("com.apollographql.federation:federation-graphql-java-support:2.1.0")
    api(project(":utilitymodule"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

