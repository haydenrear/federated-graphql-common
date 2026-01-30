
plugins {
    id("com.hayden.test-jar")
    id("com.hayden.graphql")
    id("com.hayden.no-main-class")
}

group = "com.hayden"
version = "0.0.1-SNAPSHOT"

tasks.register("prepareKotlinBuildScriptModel")

var utilLib = ""

if (project.parent?.name?.contains("multi_agent_ide_parent") ?: false) {
    utilLib = ":multi_agent_ide_java_parent"
} else {
    utilLib = ""
}


dependencies {
    implementation(project("${utilLib}:utilitymodule"))
}
