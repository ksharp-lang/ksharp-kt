plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":typesystem"))
    implementation(project(":nodes"))
    implementation(project(":test"))
    testImplementation(libs.kotest)
}
