plugins {
    kotlin("jvm")
    java
}

repositories {
    mavenCentral()
}

val graalVersion = "22.3.0"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":nodes"))
    implementation(project(":semantics"))
    implementation(project(":typesystem"))
    implementation(project(":module"))

    implementation("org.graalvm.truffle:truffle-api:$graalVersion")
    annotationProcessor("org.graalvm.truffle:truffle-dsl-processor:$graalVersion")

    testImplementation(project(":parser"))
    testImplementation(project(":test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
}


tasks {
    withType<Test> {
        jvmArgs = listOf(
            "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api=ALL-UNNAMED",
            "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.nodes=ALL-UNNAMED",
            "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.staticobject=ALL-UNNAMED",
            "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.dsl=ALL-UNNAMED",
            "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.frame=ALL-UNNAMED"
        )
    }
}
