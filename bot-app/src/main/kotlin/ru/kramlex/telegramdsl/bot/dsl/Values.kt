package ru.kramlex.telegramdsl.bot.dsl

import ru.kramlex.telegramdsl.bot.dsl.actions.*


sealed class Values<out E : ActionExecutor<State, SaveType>> {

    data class Any<out E : ActionExecutor<State, SaveType>>(
        val actions: List<Action>
    ) : Values<E>() {
        @StateDslMarker
        constructor(
            executor: E,
            lambda: LineListBuilder<Action, E>.() -> Unit
        ) :
                this(LineListBuilder<Action, E>(executor).apply(lambda).build())
    }

    data class Many<out E : ActionExecutor<State, SaveType>>(
        val map: Map<String, List<Action>>,
        val errorAction: Action.SendMessage
    ) : Values<E>() {
        @StateDslMarker
        constructor(
            executor: E,
            errorAction: Action.SendMessage,
            lambda: MapListBuilder<Action, E>.() -> Unit
        ) : this(
            map = MapListBuilder<Action, E>(executor).apply(lambda).build(),
            errorAction = errorAction
        )

        fun getAction(text: String): List<Action> {
            return map.getValue(text)
        }
    }

    data class Regex<out E : ActionExecutor<State, SaveType>>(
        val regex: String,
        val actions: List<Action>,
        val errorAction: Action.SendMessage
    ) : Values<E>() {
        @StateDslMarker
        constructor(
            executor: E,
            regex: String,
            errorAction: Action.SendMessage,
            lambda: LineListBuilder<Action, E>.() -> Unit
        ) : this(
            regex = regex,
            actions = LineListBuilder<Action, E>(executor).apply(lambda).build(),
            errorAction = errorAction
        )
    }

    data class Constant<out E : ActionExecutor<State, SaveType>>(
        val string: String,
        val actions: List<Action>,
        val errorAction: Action.SendMessage
    ) : Values<E>() {
        @StateDslMarker
        constructor(
            executor: E,
            string: String,
            errorAction: Action.SendMessage,
            lambda: LineListBuilder<Action, E>.() -> Unit
        ) : this(
            string = string,
            actions = LineListBuilder<Action, E>(executor).apply(lambda).build(),
            errorAction = errorAction
        )
    }

    fun validate(text: String): Action? {
        return when (this) {
            is Any -> null
            is Constant -> {
                if (text != this.string) errorAction
                else null
            }
            is Many -> {
                map.forEach {
                    if (it.key == text) return null
                }
                return errorAction
            }
            is Regex -> {
                if (regex.toRegex().containsMatchIn(text)) null
                else errorAction
            }
        }
    }
}

@StateDslMarker
class MapListBuilder<out T : Action, out E : ActionExecutor<State, SaveType>>(
    private val executor: E
) {
    private val actionsMap: MutableMap<String, List<T>> = mutableMapOf()

    fun addValue(string: String, lambda: LineListBuilder<T, E>.() -> Unit) {
        actionsMap[string] = LineListBuilder<T, E>(executor).apply(lambda).build()
    }

    fun build(): Map<String, List<T>> = actionsMap.toMap()
}


@StateDslMarker
class LineListBuilder<out T : Any, out E : ActionExecutor<State, SaveType>>(
    private val executor: E
) {
    private val mutableList: MutableList<T> = mutableListOf()

    @StateDslMarker
    fun addAction(action: E.() -> @UnsafeVariance T) {
        mutableList.add(action(executor))
    }

    @StateDslMarker
    fun addActions(lambda: E.() -> List<@UnsafeVariance T>) {
        mutableList.addAll(lambda.invoke(executor))
    }

    @StateDslMarker
    fun actionsBuilder(lambda: MultipleLineListBuilder<T, E>.() -> Unit) {
        mutableList.addAll(
            MultipleLineListBuilder<T, E>(executor)
                .apply(lambda)
                .build()
        )
    }

    fun build(): List<T> = mutableList.toList()
}

@StateDslMarker
class MultipleLineListBuilder<out T : Any, out E : ActionExecutor<State, SaveType>>(
    private val executor: E
) {
    private val mutableList: MutableList<@UnsafeVariance T> = mutableListOf()

    @StateDslMarker
    fun addAction(lambda: E.() -> @UnsafeVariance T) {
        mutableList.add(executor.lambda())
    }

    fun build(): List<T> = mutableList.toList()
}
