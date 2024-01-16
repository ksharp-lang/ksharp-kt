plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":compiler"))
    implementation(project(":module"))
    implementation(project(":ir"))
    implementation(project(":typesystem"))
    implementation(project(":parser"))
    implementation(project(":nodes"))
    implementation(project(":semantics"))
    implementation(libs.graalvm.truffle.api)
    testImplementation(libs.kotest)
    testImplementation(project(":test"))
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

tasks.register<JavaExec>("compile-kore-library") {
    group = "kore"
    description = "Compile the kore library"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.ksharp.kore.CompileLibraryKt")
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
