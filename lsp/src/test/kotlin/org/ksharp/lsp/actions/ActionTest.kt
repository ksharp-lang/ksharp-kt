package org.ksharp.lsp.actions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class ActionTest : StringSpec({
    "Action basics" {
        action<String, String>(
            "action 1",
            "Cancelled"
        ) {
            execution { _, payload ->
                payload
            }
        }("Test")
            .apply {
                cancelled.shouldBeFalse()
                value.get().shouldBe("Test")
            }
    }
    "Cancelable action" {
        action<String, String>(
            "action 1",
            "Cancelled"
        ) {
            execution { _, payload ->
                Thread.sleep(100)
                payload
            }
        }("Test")
            .apply {
                cancel()
                cancelled.shouldBeTrue()
                value.get().shouldBe("Cancelled")
            }
    }
    "DependsOn action" {
        val dep = action(
            "dep1",
            "Action Dep Cancelled 1"
        ) {
            execution { _, payload ->
                Thread.sleep(1000)
                payload
            }
        }
        action<String, String>(
            "action 1",
            "Cancelled",
        ) {
            dependsOn {
                +dep
            }
            execution { state, _ ->
                state["dep1"] as String
            }
        }("Test")
            .apply {
                dep("Dep Value")
                cancelled.shouldBeFalse()
                value.get().shouldBe("Dep Value")
            }
    }
    "DependsOn action first time" {
        val dep1 = action(
            "dep1",
            "Action Dep Cancelled 1"
        ) {
            execution { _, payload ->
                Thread.sleep(1000)
                payload
            }
        }
        action<String, String>(
            "action 1",
            "Cancelled"
        ) {
            dependsOn {
                +dep1
            }
            execution { state, _ ->
                state["dep1"] as String
            }
        }("Test")
            .apply {
                dep1("Test").cancel()
                cancelled.shouldBeFalse()
                value.get().shouldBe("Action Dep Cancelled 1")
            }
    }
    "Trigger action" {
        val triggerAction = action<String, Int>(
            "dep1",
            0
        ) {
            execution { _, payload ->
                payload.length
            }
        }
        action<String, String>(
            "action 1",
            "Cancelled"
        ) {
            trigger {
                +triggerAction
            }
            execution { _, payload ->
                payload
            }
        }("Test")
            .apply {
                cancelled.shouldBeFalse()
                value.get()
                triggerAction.value.get().shouldBe(4)
            }
    }
})