package org.ksharp.common

import org.ksharp.common.annotation.Mutable

typealias Action<State, Payload, Result> = (state: State, payload: Payload) -> Result
typealias ToValueAction<State, Value> = (state: State) -> Value

interface ActionKey

enum class BaseActions : ActionKey {
    Add,
}

enum class MapActions : ActionKey {
    ContainsKey
}

@Mutable
class ActionsBuilder<State> {
    private val actionTable = mutableMapOf<ActionKey, Pair<Boolean, Action<State, Any, Any>>>()

    @Suppress("UNCHECKED_CAST")
    fun <Payload, Result> query(key: ActionKey, action: Action<State, Payload, Result>) {
        actionTable[key] = false to action as Action<State, Any, Any>
    }

    @Suppress("UNCHECKED_CAST")
    fun <Payload> mutator(key: ActionKey, action: Action<State, Payload, State>) {
        actionTable[key] = true to action as Action<State, Any, Any>
    }

    internal fun build() = actionTable.toMap()
}

@Mutable
class Builder<T, Value>(
    private var state: T,
    private val toValue: ToValueAction<T, Value>,
    private val actions: ActionsBuilder<T>.() -> Unit
) {
    private val actionsTable by lazy { ActionsBuilder<T>().apply(actions).build() }
    val value: Value get() = toValue(state)

    @Suppress("UNCHECKED_CAST")
    operator fun <Result> invoke(key: ActionKey, payload: Any): Result {
        actionsTable[key]!!.let { (isMutator, action) ->
            val newResult = action(state, payload)
            if (isMutator) {
                state = newResult as T
                return Unit as Result
            }
            return newResult as Result
        }
    }
}

@Mutable
fun <T, Value> builder(
    state: T,
    toValue: ToValueAction<T, Value>,
    actions: ActionsBuilder<T>.() -> Unit
) = Builder(state, toValue, actions)

@Mutable
fun <Item> listBuilder(actions: ActionsBuilder<MutableList<Item>>.() -> Unit = {}) = builder(
    mutableListOf(),
    MutableList<Item>::toList
) {
    mutator<Item>(BaseActions.Add) { state, payload ->
        state.apply {
            add(payload)
        }
    }
    actions()
}

fun <V> Builder<MutableList<V>, List<V>>.add(value: V) =
    this<Unit>(BaseActions.Add, value as Any)

@Mutable
fun <Key, Value> mapBuilder(actions: ActionsBuilder<MutableMap<Key, Value>>.() -> Unit = {}) = builder(
    mutableMapOf<Key, Value>(),
    { it.toMap() }
) {
    mutator<Pair<Key, Value>>(BaseActions.Add) { state, (key, value) ->
        state.apply {
            put(key, value)
        }
    }
    query<Key, Boolean>(MapActions.ContainsKey) { state, key ->
        state.containsKey(key)
    }
    actions()
}

fun <K, V> Builder<MutableMap<K, V>, Map<K, V>>.containsKey(key: K) =
    this<Boolean>(MapActions.ContainsKey, key as Any)

fun <K, V> Builder<MutableMap<K, V>, Map<K, V>>.add(value: Pair<K, V>) =
    this<Unit>(BaseActions.Add, value as Any)