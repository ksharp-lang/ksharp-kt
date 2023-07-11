plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":test"))
    implementation(project(":nodes"))
    testImplementation(libs.kotest)
}
