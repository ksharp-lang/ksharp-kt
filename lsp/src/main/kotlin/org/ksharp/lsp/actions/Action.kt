package org.ksharp.lsp.actions

import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Collectors

class ActionState(
    private val _cancelled: AtomicBoolean,
    private val dependencies: Map<String, Any>
) {

    val cancelled: Boolean get() = _cancelled.get()

    operator fun get(actionId: String) = dependencies[actionId]

}

class ActionExecution<Output>(
    val value: CompletableFuture<Output>,
    private val _cancelled: AtomicBoolean
) {
    val cancelled: Boolean get() = _cancelled.get()

    fun cancel() {
        _cancelled.set(true)
    }
}

class Action<Payload, Output>(
    private val id: String,
    private val whenCancelledOutput: Output,
    private val trigger: List<Action<Output, *>> = listOf(),
    private val dependsOn: List<Action<*, *>> = listOf(),
    private val execution: (ActionState, Payload) -> Output,
) {
    var value: CompletableFuture<Output> = CompletableFuture()
        private set

    private fun begin() {
        value = CompletableFuture()
    }

    operator fun invoke(payload: Payload): ActionExecution<Output> {
        if (value.isDone) {
            begin()
        }

        val actions = dependsOn.stream().map { action ->
            action.value.thenApplyAsync { action.id to it!! }
        }.collect(Collectors.toList())

        trigger.forEach {
            it.begin()
        }

        val cancelled = AtomicBoolean(false)

        CompletableFuture.allOf(*actions.toTypedArray())
            .thenApplyAsync {
                val dependencies = actions.associate {
                    it.get()
                }
                val actionState = ActionState(cancelled, dependencies)
                val result = execution(actionState, payload)

                if (cancelled.get()) value.complete(whenCancelledOutput)
                else {
                    value.complete(result)
                    triggerActions(result)
                }
            }

        return ActionExecution(
            value,
            cancelled
        )
    }

    private fun triggerActions(result: Output) {
        trigger.forEach {
            it(result)
        }
    }
}

fun <Payload, Output> action(
    id: String,
    whenCancelledOutput: Output,
    builder: ActionsBuilder<Payload, Output>.() -> Unit
): Action<Payload, Output> =
    ActionsBuilder<Payload, Output>(id, whenCancelledOutput)
        .apply(builder)
        .build()