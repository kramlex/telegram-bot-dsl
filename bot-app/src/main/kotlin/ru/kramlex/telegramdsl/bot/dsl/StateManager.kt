package ru.kramlex.telegramdsl.bot.dsl

import ru.kramlex.telegramdsl.bot.dsl.actions.ActionExecutor
import ru.kramlex.telegramdsl.bot.dsl.actions.SaveType
import ru.kramlex.telegramdsl.bot.dsl.actions.State
import kotlin.reflect.KClass

class StateManager<out T : Any, out E: ActionExecutor<State, SaveType>>(
    private val actionExecutor: E
) {
    private val _stateMap: MutableMap<KClass<out T>, StateInfo<E>> = mutableMapOf()

    fun getState(className: @UnsafeVariance T): StateInfo<E>? {
        return _stateMap[className::class]
    }

    @StateDslMarker
    fun states(lambda: Builder<T,E>.(ActionExecutor<State, SaveType>) -> Unit) {
        _stateMap.plusAssign(
            Builder<T,E>(actionExecutor)
                .apply { lambda(actionExecutor) }
                .build()
        )
    }

    @StateDslMarker
    class Builder<out T: Any, out E: ActionExecutor<State, SaveType>>(
        private val executor: E
    ) {
        private val mutableMap: MutableMap<KClass<out T>, StateInfo<E>> = mutableMapOf()

        @StateDslMarker
        fun addState(state: @UnsafeVariance T, lambda: StateInfo.Builder<E>.(T) -> Unit) {
            putInfo(state::class, StateInfo.Builder(executor).apply { lambda(state) }.build())
        }

        private fun putInfo(stateKClass: KClass<out T>, info: StateInfo<E>) {
            mutableMap[stateKClass] = info
        }

        fun build(): MutableMap<KClass<out @UnsafeVariance T>, StateInfo<@UnsafeVariance E>> = mutableMap
    }
}
