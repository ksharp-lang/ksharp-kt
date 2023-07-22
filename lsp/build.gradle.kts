plugins {
    kotlin("jvm")
    alias(libs.plugins.shadow)
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

koverReport {
    filters {
        excludes {
            classes("org.ksharp.lsp.KsLspMain")
        }
    }
}
