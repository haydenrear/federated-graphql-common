plugins {
    id("com.hayden.test-jar")
    id("com.hayden.graphql")
    id("com.hayden.no-main-class")
}

group = "com.hayden"
version = "0.0.1-SNAPSHOT"

tasks.register("prepareKotlinBuildScriptModel")

dependencies {
    api(project(":utilitymodule"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

tasks.generateJava {
    schemaPaths.add("${projectDir}/src/main/resources/graphql")
    packageName = "com.hayden.gateway.codegen"
    generateClient = true
}

