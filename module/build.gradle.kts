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
    implementation(project(":test"))
    implementation("io.netty:netty-buffer:4.1.93.Final")
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
}