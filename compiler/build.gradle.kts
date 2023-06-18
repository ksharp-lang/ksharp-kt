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
    implementation(project(":common"))
    implementation(project(":parser"))
    implementation(project(":nodes"))
    implementation(project(":module"))
    implementation(project(":semantics"))
    implementation(project(":typesystem"))
    testImplementation(project(":test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
}

tasks.test {
    useJUnitPlatform()
}