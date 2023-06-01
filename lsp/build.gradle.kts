plugins {
    kotlin("jvm")
}

group = "org.ksharp"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j:0.21.0")
    implementation("org.eclipse.lsp4j:org.eclipse.lsp4j.jsonrpc:0.21.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
