
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
