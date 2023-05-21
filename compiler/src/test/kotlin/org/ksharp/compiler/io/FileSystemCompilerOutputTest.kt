package org.ksharp.compiler.io

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.charset.StandardCharsets

private val outputFile = File("test-output").absoluteFile

class FileSystemCompilerOutputTest : StringSpec({
    "Write content" {
        val compilerOutput = FileSystemCompilationOutput(outputFile.toPath())
        compilerOutput.write("test/name.txt", "Test")
        outputFile.resolve("test/name.txt").apply {
            exists().shouldBeTrue()
            readText(StandardCharsets.UTF_8).shouldBe("Test")
        }
    }
}) {

    override suspend fun beforeAny(testCase: TestCase) {
        outputFile.mkdirs()
    }

    override suspend fun afterAny(testCase: TestCase, result: TestResult) {
        outputFile.deleteRecursively()
    }
}