plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":test"))
    implementation(project(":typesystem"))
    testImplementation(libs.kotest)
}
