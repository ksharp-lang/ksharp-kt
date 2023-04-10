plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.netty:netty-buffer:4.1.91.Final")
    testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
}