plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    implementation(project(":parser"))
    implementation(project(":nodes"))
    implementation(project(":module"))
    implementation(project(":semantics"))
    implementation(project(":ir"))
    implementation(project(":typesystem"))
    testImplementation(project(":test"))
    testImplementation(libs.kotest)
}
