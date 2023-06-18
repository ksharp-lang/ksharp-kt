rootProject.name = "ksharp-kt"

pluginManagement {
    plugins {
        kotlin("jvm") version "1.8.22" apply false
        kotlin("kapt") version "1.8.22" apply false
        kotlin("plugin.serialization") version "1.8.22" apply false
        id("org.sonarqube") version "4.2.1.3168"
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
include("compiler")
include("lsp")
