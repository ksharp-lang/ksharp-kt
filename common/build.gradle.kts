plugins {
    kotlin("jvm")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.netty.buffer)
    testImplementation(libs.kotest)
}
