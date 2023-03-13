rootProject.name = "ksharp-kt"

pluginManagement {
    plugins {
        kotlin("jvm") version "1.7.21" apply false
        kotlin("kapt") version "1.7.21" apply false
        kotlin("plugin.serialization") version "1.7.21" apply false
        id("org.sonarqube") version "3.5.0.2730"
        jacoco
    }
}

include("typesystem")
include("common")
include("test")
include("parser")
include("nodes")
include("semantics")
include("module")
