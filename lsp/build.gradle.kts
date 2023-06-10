plugins {
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.ksharp"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.21.0")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.21.0")
    implementation(project(":nodes"))
    implementation(project(":parser"))
    implementation(project(":common"))
    implementation(project(":semantics"))
    implementation(project(":module"))
    implementation(project(":typesystem"))
    testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
}

tasks.test {
    useJUnitPlatform()
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
