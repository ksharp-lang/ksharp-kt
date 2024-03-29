plugins {
    kotlin("jvm")
    java
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":nodes"))
    implementation(project(":typesystem"))
    implementation(project(":module"))

    implementation(libs.graalvm.truffle.api)
    annotationProcessor(libs.graalvm.truffle.dsl.processor)

    testImplementation(project(":semantics"))
    testImplementation(project(":parser"))
    testImplementation(project(":test"))
    testImplementation(libs.kotest)
}

//--add-exports
//org.graalvm.truffle/com.oracle.truffle.api=ALL-UNNAMED
//--add-exports
//org.graalvm.truffle/com.oracle.truffle.api.nodes=ALL-UNNAMED
//--add-exports
//org.graalvm.truffle/com.oracle.truffle.api.strings=ALL-UNNAMED
//--add-exports
//org.graalvm.truffle/com.oracle.truffle.api.staticobject=ALL-UNNAMED
//--add-exports
//org.graalvm.truffle/com.oracle.truffle.api.dsl=ALL-UNNAMED
//--add-exports
//org.graalvm.truffle/com.oracle.truffle.api.frame=ALL-UNNAMED
//--add-exports
//org.graalvm.truffle/com.oracle.truffle.api.interop=ALL-UNNAMED

tasks {
    withType<Test> {
        jvmArgs = listOf(
            "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api=ALL-UNNAMED",
            "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.nodes=ALL-UNNAMED",
            "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.strings=ALL-UNNAMED",
            "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.staticobject=ALL-UNNAMED",
            "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.dsl=ALL-UNNAMED",
            "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.frame=ALL-UNNAMED",
            "--add-exports", "org.graalvm.truffle/com.oracle.truffle.api.interop=ALL-UNNAMED"
        )
    }
}
