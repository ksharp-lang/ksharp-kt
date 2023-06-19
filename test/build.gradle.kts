plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation("io.kotest:kotest-runner-junit5:5.6.2")
    testImplementation("io.kotest:kotest-runner-junit5:5.6.2")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()

}