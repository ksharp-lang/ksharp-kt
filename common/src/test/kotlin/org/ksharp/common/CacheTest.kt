package org.ksharp.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.atomic.AtomicInteger

class CacheTest : StringSpec({
    "Cache test" {
        val hits = AtomicInteger(0)
        val cache = cacheOf<String, String>()
        cache.get("Key1") {
            hits.incrementAndGet()
            "Value 1"
        }.shouldBe("Value 1")
        cache.get("Key1") {
            hits.incrementAndGet()
            "Value 2"
        }.shouldBe("Value 1")
        hits.get().shouldBe(1)
    }
})