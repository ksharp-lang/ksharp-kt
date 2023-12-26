plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":nodes"))
    implementation(project(":module"))
    implementation(project(":typesystem"))

    testImplementation(project(":semantics"))
    testImplementation(project(":parser"))
    testImplementation(project(":test"))
    testImplementation(libs.kotest)
}
