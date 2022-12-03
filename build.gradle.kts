allprojects {
    val project = this

    apply(plugin = "jacoco")

    group = "org.ksharp"
    version = "1.0.0"

    System.getenv("CUSTOM_GRADLE_BUILD_DIR")?.run {
        project.buildDir = File("$this/ksharp-kt/${project.name}")
    }

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
                xml.required.set(false)
                csv.required.set(false)
                html.outputLocation.set(layout.buildDirectory.dir("reports/jacocoHtml"))
            }
        }
    }

    extensions.configure(JacocoPluginExtension::class) {
        this.toolVersion = "0.8.7"
        reportsDirectory.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}