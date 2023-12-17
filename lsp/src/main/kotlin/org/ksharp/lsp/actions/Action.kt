package org.ksharp.lsp.actions

import org.ksharp.common.cast
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import java.util.stream.Collectors
import kotlin.concurrent.withLock

data class ActionId<Output>(
    val id: String
)

class Actions(
    private val actions: Map<ActionId<*>, Action<*, *>>,
) {
    operator fun <Payload, Output> invoke(state: ActionExecutionState, actionId: ActionId<Output>, payload: Payload) {
        actions[actionId]?.let { it.cast<Action<Payload, Output>>()(state, payload) }
    }
}

class ActionExecutionState {
    private val lock = ReentrantLock()
    private val executions = mutableMapOf<ActionId<*>, CompletableFuture<Any>>()
    private val _canceled = AtomicBoolean(false)

    val canceled get() = _canceled.get()

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(actionId: ActionId<T>): CompletableFuture<T> =
        lock.withLock {
            (executions[actionId] ?: CompletableFuture<Any>().also {
                executions[actionId] = it
            }) as CompletableFuture<T>
        }

    fun cancel() {
        _canceled.set(true)
    }

    fun <Output> resetActionState(id: ActionId<Output>): CompletableFuture<Output> =
        executions.remove(id)!!.cast()
}

class ActionState(
    private val execution: ActionExecutionState,
    private val dependencies: Map<ActionId<out Any?>, Any>
) {

    val canceled: Boolean get() = execution.canceled

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(actionId: ActionId<T>): T = dependencies[actionId] as T

}

class Action<Payload, Output>(
    private val id: ActionId<Output>,
    private val whenCancelledOutput: Output,
    private val trigger: List<Action<Output, *>> = listOf(),
    private val dependsOn: List<Action<*, *>> = listOf(),
    private val execution: (ActionState, Payload) -> Output,
) {
    operator fun invoke(execution: ActionExecutionState, payload: Payload): CompletableFuture<Output> {
        val actions = dependsOn.stream().map { action ->
            execution[action.id].thenApplyAsync { action.id to it!! }
        }.collect(Collectors.toList())

        val future = execution[id]
        CompletableFuture.allOf(*actions.toTypedArray())
            .thenApplyAsync {
                val dependencies = actions.associate {
                    it.get()
                }
                val actionState = ActionState(execution, dependencies)

                try {
                    val result = this.execution(actionState, payload)
                    if (execution.canceled) future.complete(whenCancelledOutput)
                    else {
                        future.complete(result)
                        triggerActions(execution, result)
                    }
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                    throw e
                }
            }

        return future
    }

    private fun triggerActions(execution: ActionExecutionState, result: Output) {
        trigger.forEach {
            it(execution, result)
        }
    }
}
