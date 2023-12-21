plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":common"))
    
    testImplementation(project(":test"))
    testImplementation(libs.kotest)
}
