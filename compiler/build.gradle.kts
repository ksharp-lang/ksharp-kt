plugins {
    kotlin("jvm")
}

group = "org.ksharp"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":parser"))
    implementation(project(":semantics"))
    testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
}

tasks.test {
    useJUnitPlatform()
}