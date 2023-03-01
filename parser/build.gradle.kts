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
    implementation(project(":nodes"))
    testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
}