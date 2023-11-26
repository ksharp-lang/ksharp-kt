plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":parser"))
    implementation(project(":nodes"))
    implementation(project(":module"))
    implementation(project(":semantics"))
    implementation(project(":ir"))
    implementation(project(":typesystem"))
    testImplementation(project(":test"))
    testImplementation(libs.kotest)
}

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
