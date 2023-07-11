rootProject.name = "ksharp-kt"

pluginManagement {
    val kotlinVersion = "1.9.0"
    val sonarqubeVersion = "4.2.1.3168"

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        kotlin("kapt") version kotlinVersion apply false
        kotlin("plugin.serialization") version kotlinVersion apply false
        id("org.sonarqube") version sonarqubeVersion
        jacoco
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.0")
            version("kotest", "5.6.2")
            version("eclipse.lsp4j", "0.21.0")
            version("graalvm", "22.3.0")
            version("netty", "4.1.93.Final")

            // Tooling
            version("jacoco", "0.8.10")
            version("plugin.com.github.johnrengelman.shadow", "8.1.1")

            // Plugins
            plugin("shadow", "com.github.johnrengelman.shadow").versionRef("plugin.com.github.johnrengelman.shadow")

            // Libraries
            library("kotest", "io.kotest", "kotest-runner-junit5").versionRef("kotest")
            library("eclipse.lsp4j", "org.eclipse.lsp4j", "org.eclipse.lsp4j").versionRef("eclipse.lsp4j")
            library(
                "eclipse.lsp4j.jsonrpc",
                "org.eclipse.lsp4j",
                "org.eclipse.lsp4j.jsonrpc"
            ).versionRef("eclipse.lsp4j")
            library("graalvm.truffle.api", "org.graalvm.truffle", "truffle-api").versionRef("graalvm")
            library(
                "graalvm.truffle.dsl-processor",
                "org.graalvm.truffle",
                "truffle-dsl-processor"
            ).versionRef("graalvm")
            library("netty.buffer", "io.netty", "netty-buffer").versionRef("netty")
        }
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
include("ir")
