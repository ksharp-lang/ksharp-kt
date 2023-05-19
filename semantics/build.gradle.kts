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
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation(project(":test"))
    testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
}
