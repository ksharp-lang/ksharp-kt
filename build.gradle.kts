plugins {
    base
    id("org.sonarqube")
    id("jacoco-report-aggregation")
}

repositories {
    mavenCentral()
}

sonarqube {
    properties {
        property("sonar.projectKey", "ksharp-lang_ksharp-kt")
        property("sonar.organization", "ksharp-lang")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

dependencies {
    subprojects.forEach {
        jacocoAggregation(project(":${it.name}"))
    }
}

reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) {
            testType.set(TestSuiteType.UNIT_TEST)
        }
    }
}

tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport"))
}

allprojects {
    val project = this

    group = "org.ksharp"
    version = "1.0.0"

    System.getenv("CUSTOM_GRADLE_BUILD_DIR")?.run {
        project.buildDir = File("$this/ksharp-kt/${project.name}")
    }
}

subprojects {
    apply(plugin = "jacoco")

    tasks {
        withType<Test> {
            useJUnitPlatform()
            configure<JacocoTaskExtension> {
                isEnabled = true
                setDestinationFile(layout.buildDirectory.file("jacoco/${name}.exec").get().asFile)
            }
        }
        withType<JacocoReport> {
            reports.apply {
                xml.required.set(true)
                csv.required.set(true)
                html.outputLocation.set(layout.buildDirectory.dir("reports/jacocoHtml"))
            }
        }
    }

    extensions.configure(JacocoPluginExtension::class) {
        this.toolVersion = "0.8.8"
        reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}