package org.ksharp.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.util.concurrent.atomic.AtomicInteger

private interface Listener {
    fun somethingHappened()
}

class ListenersTest : StringSpec({
    "Add listeners" {
        val counter = AtomicInteger()
        val listeners = Listeners<Listener>()
        listeners.add(object : Listener {
            override fun somethingHappened() {
                counter.incrementAndGet()
            }
        })
        listeners { somethingHappened() }
        counter.get().shouldBe(1)
    }
})