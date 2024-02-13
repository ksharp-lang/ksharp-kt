rootProject.name = "ksharp-kt"

pluginManagement {
    val kotlinVersion = "1.9.22"
    val sonarqubeVersion = "4.4.1.3373"
    val koverVersion = "0.7.5"

    plugins {
        kotlin("jvm") version kotlinVersion apply false
        id("org.sonarqube") version sonarqubeVersion
        id("org.jetbrains.kotlinx.kover") version koverVersion
    }

    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotest", "5.8.0")
            version("eclipse.lsp4j", "0.21.2")
            version("graalvm", "22.3.4")
            version("netty", "4.1.106.Final")
            version("reflections", "0.10.2")

            // Tooling
            version("plugin.com.github.johnrengelman.shadow", "8.1.1")
            version("plugin.org.jetbrains.kotlinx.kover", "0.7.5")
            version("plugin.org.graalvm.buildtools.native", "0.10.0")

            // Plugins
            plugin("shadow", "com.github.johnrengelman.shadow").versionRef("plugin.com.github.johnrengelman.shadow")
            plugin("graalvm", "org.graalvm.buildtools.native").versionRef("plugin.org.graalvm.buildtools.native")

            // Libraries
            library(
                "kover",
                "org.jetbrains.kotlinx",
                "kover-gradle-plugin"
            ).versionRef("plugin.org.jetbrains.kotlinx.kover")
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
            library("reflections", "org.reflections", "reflections").versionRef("reflections")
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
include("docs")
include("kore")
