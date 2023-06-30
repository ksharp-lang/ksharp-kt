plugins {
    kotlin("jvm")
    java
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":nodes"))
    implementation(project(":semantics"))
    implementation(project(":typesystem"))
    implementation(project(":module"))

    implementation("org.graalvm.truffle:truffle-api:23.0.0")
    annotationProcessor("org.graalvm.truffle:truffle-dsl-processor:23.0.0")

    testImplementation(project(":parser"))
    testImplementation(project(":test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
}

tasks {
    withType<JavaCompile> {
        options.compilerArgs.apply {
            addAll(
                listOf(
                    "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api=ALL-UNNAMED",
                    "--add-exports", "org.graalvm.truffle/com.oracle.truffle.nodes=ALL-UNNAMED",
                    "--add-exports", "org.graalvm.truffle/com.oracle.truffle.staticobject=ALL-UNNAMED",
                    "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.dsl=ALL-UNNAMED",
                    "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.frame=ALL-UNNAMED"
                )
            )
        }
    }
}
