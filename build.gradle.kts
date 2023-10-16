plugins {
    base
    id("org.sonarqube")
    id("org.jetbrains.kotlinx.kover")
}

repositories {
    mavenCentral()
}

sonarqube {
    properties {
        property("sonar.projectKey", "ksharp-lang_ksharp-kt")
        property("sonar.organization", "ksharp-lang")
        property("sonar.host.url", "https://sonarcloud.io")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${project.buildDir}/reports/kover/report.xml"
        )
    }
}

dependencies {
    subprojects.forEach {
        kover(project(":${it.name}"))
    }
}

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.kover)
    }
}

allprojects {
    val project = this

    group = "org.ksharp"
    version = "1.0.0"

    System.getenv("CUSTOM_GRADLE_BUILD_DIR")?.run {
        project.buildDir = File("$this/ksharp-kt/${project.name}")
    }

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlinx.kover")

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }
    }

    koverReport {
        filters {
            excludes {
                annotatedBy(
                    "org.ksharp.common.annotation.KoverIgnore"
                )
            }
        }
    }
}
