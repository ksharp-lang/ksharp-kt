plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":typesystem"))
    implementation(project(":nodes"))
    implementation(project(":module"))
    testImplementation(project(":test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
}
