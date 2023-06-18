plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":test"))
    implementation(project(":typesystem"))
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
}