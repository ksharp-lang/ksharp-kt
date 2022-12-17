plugins {
    id("org.sonarqube")
}

sonarqube {
    properties {
        property("sonar.projectKey", "ksharp-lang_ksharp-kt")
        property("sonar.organization", "ksharp-lang")
        property("sonar.host.url", "https://sonarcloud.io")
    }
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
                xml.outputLocation.set(layout.buildDirectory.file("reports/jacocoXml.xml"))
                csv.outputLocation.set(layout.buildDirectory.file("reports/jacocoCSV.csv"))
                html.outputLocation.set(layout.buildDirectory.dir("reports/jacocoHtml"))
            }
        }
    }

    extensions.configure(JacocoPluginExtension::class) {
        this.toolVersion = "0.8.7"
        reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}