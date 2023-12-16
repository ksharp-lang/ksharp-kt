plugins {
    kotlin("jvm")
    alias(libs.plugins.shadow)
    alias(libs.plugins.graalvm)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.eclipse.lsp4j)
    implementation(libs.eclipse.lsp4j.jsonrpc)
    implementation(project(":nodes"))
    implementation(project(":parser"))
    implementation(project(":common"))
    implementation(project(":semantics"))
    implementation(project(":module"))
    implementation(project(":typesystem"))
    testImplementation(libs.kotest)
    testImplementation(libs.reflections)
}

graalvmNative {
    toolchainDetection.set(true)
    binaries {
        named("main") {
            imageName.set("ks-lsp")
            mainClass.set("org.ksharp.lsp.KsLspMain")
        }
        named("test") {
            imageName.set("ks-lsp-test")
            buildArgs.addAll("--verbose", "-O0")
        }
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "org.ksharp.lsp.KsLspMain"
    }
    archiveFileName.set("ks-lsp.jar")
}

tasks.shadowJar {
    archiveFileName.set("ks-lsp-all.jar")
}

tasks.nativeCompile {
    classpathJar.set(tasks.shadowJar.flatMap { it.archiveFile })
}
