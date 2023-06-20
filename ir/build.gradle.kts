plugins {
    kotlin("jvm")
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
    testImplementation(project(":test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
}
