plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":typesystem"))
    implementation(project(":nodes"))
    implementation(project(":module"))
    testImplementation(project(":test"))
    testImplementation(project(":parser"))
    testImplementation(libs.kotest)
}
