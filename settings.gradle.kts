rootProject.name = "ksharp-kt"

pluginManagement {
    val kotlinVersion = "1.9.0"
    val sonarqubeVersion = "4.2.1.3168"
    val koverVersion = "0.7.2"

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        id("org.sonarqube") version sonarqubeVersion
        id("org.jetbrains.kotlinx.kover") version koverVersion
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotest", "5.6.2")
            version("eclipse.lsp4j", "0.21.0")
            version("graalvm", "22.3.0")
            version("netty", "4.1.94.Final")

            // Tooling
            version("plugin.com.github.johnrengelman.shadow", "8.1.1")
            version("plugin.org.jetbrains.kotlinx.kover", "0.7.2")

            // Plugins
            plugin("shadow", "com.github.johnrengelman.shadow").versionRef("plugin.com.github.johnrengelman.shadow")

            // Libraries
            library("kover", "org.jetbrains.kotlinx","kover-gradle-plugin").versionRef("plugin.org.jetbrains.kotlinx.kover")
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
