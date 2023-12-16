package org.ksharp.lsp.actions

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe

class ActionTest : StringSpec({
    val action1 = ActionId<String>("action 1")
    val dep1 = ActionId<String>("dep1")
    val dep1Int = ActionId<Int>("dep1")
    "Action basics" {
        actions {
            action(action1, "Cancelled") {
                execution { _, payload ->
                    payload
                }
            }
        }.apply {
            val state = ActionExecutionState()
            this(state, action1, "Test")
            state.canceled.shouldBeFalse()
            state[action1].get().shouldBe("Test")
        }
    }
    "Cancelable action" {
        actions {
            action(
                action1,
                "Cancelled"
            ) {
                execution { _, payload ->
                    Thread.sleep(100)
                    payload
                }
            }
        }.apply {
            val state = ActionExecutionState()
            this(state, action1, "Test")
            state.cancel()
            state.canceled.shouldBeTrue()
            state[action1].get().shouldBe("Cancelled")
        }
    }
    "DependsOn using graphBuilder" {
        actions {
            val dep = action(
                dep1,
                "Action Dep Cancelled 1"
            ) {
                execution { _, payload ->
                    Thread.sleep(1000)
                    payload
                }
            }
            action<String, String>(
                action1,
                "Cancelled",
            ) {
                graphBuilder {
                    dependsOn {
                        +dep
                    }
                    execution { state, _ ->
                        state[dep1]
                    }
                }
            }
        }.apply {
            val state = ActionExecutionState()
            this(state, action1, "Test")
            this(state, dep1, "Dep Value")
            state.canceled.shouldBeFalse()
            state[dep1].get().shouldBe("Dep Value")
            state[action1].get().shouldBe("Dep Value")
        }
    }
    "DependsOn action" {
        actions {
            val dep = action(
                dep1,
                "Action Dep Cancelled 1"
            ) {
                execution { _, payload ->
                    Thread.sleep(1000)
                    payload
                }
            }
            action<String, String>(
                action1,
                "Cancelled",
            ) {
                dependsOn {
                    +dep
                }
                execution { state, _ ->
                    state[dep1]
                }
            }
        }.apply {
            val state = ActionExecutionState()
            this(state, action1, "Test")
            this(state, dep1, "Dep Value")
            state.canceled.shouldBeFalse()
            state[dep1].get().shouldBe("Dep Value")
            state[action1].get().shouldBe("Dep Value")
        }
    }
    "DependsOn cancel state" {
        actions {
            val dep1Action = action(
                dep1,
                "Action Dep Cancelled 1"
            ) {
                execution { _, payload ->
                    Thread.sleep(1000)
                    payload
                }
            }
            action<String, String>(
                action1,
                "Cancelled"
            ) {
                dependsOn {
                    +dep1Action
                }
                execution { state, _ ->
                    state[dep1]
                }
            }
        }.apply {
            val state = ActionExecutionState()
            this(state, dep1, "Test")
            this(state, action1, "Test")
            state.cancel()
            state.canceled.shouldBeTrue()
            state[dep1].get().shouldBe("Action Dep Cancelled 1")
            state[action1].get().shouldBe("Cancelled")
        }
    }
    "Trigger action" {
        actions {
            val triggerAction = action<String, Int>(
                dep1Int,
                0
            ) {
                execution { _, payload ->
                    payload.length
                }
            }
            action(
                action1,
                "Cancelled"
            ) {
                trigger {
                    +triggerAction
                }
                execution { _, payload ->
                    payload
                }
            }
        }.apply {
            val state = ActionExecutionState()
            this(state, action1, "Test")
            state.canceled.shouldBeFalse()
            state[dep1Int].get().shouldBe(4)
        }
    }
})
