package org.ksharp.common

import io.kotest.assertions.throwables.shouldThrowMessage
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class HandlePromiseTest : StringSpec({
    "Create handle and retrieve value" {
        val handle = handlePromise<Int>()
        handle.set(10)
        handle.handle.shouldBe(10)
    }
    "Handle didn't set" {
        val handle = handlePromise<Int>()
        handle.handle.shouldBeNull()
    }
    "Try setting twice the handle value" {
        val handle = handlePromise<Int>()
        handle.set(10)
        shouldThrowMessage("Handle already set") {
            handle.set(20)
        }
        handle.handle.shouldBe(10)
    }
    "handle should be mock handle" {
        val handle = handlePromise<Int>()
        handle.shouldBe(MockHandlePromise<Int>())
    }
    "mock handle should be mock handle" {
        val handle = MockHandlePromise<Int>()
        handle.shouldBe(MockHandlePromise<Int>())
    }
    "readonly handle test" {
        val handle = handlePromise<Int>()
        handle.set(100)
        val rHandle = ReadOnlyHandlePromise(handle)
        rHandle.set(500)
        rHandle.handle
            .shouldBe(100)
        rHandle.shouldBe(handle)
    }
})
