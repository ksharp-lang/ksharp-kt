plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":typesystem"))
    testImplementation(libs.kotest)
}
