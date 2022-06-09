package ru.kramlex.telegramdsl.bot.dsl.actions

import ru.kramlex.telegramdsl.bot.dsl.StateDslMarker


sealed interface Action {
    abstract class Save(saveType: SaveType): ActionExecutor.ExecutableWithMessage
    abstract class SendMessage(val message: String) : ActionExecutor.ExecutableWithContext
    abstract class Route(val state: State) : Action, ActionExecutor.Executable
}

@StateDslMarker
class MultipleLineListBuilder<out T: Any, out E: ActionExecutor<State, SaveType>>(
    private val executor: E
) {
    private val mutableList: MutableList<@UnsafeVariance T> = mutableListOf()

    fun sendTextMessage(message: String) {
        mutableList.add(executor.sendTextMessage(message) as T)
    }

    fun <K: SaveType> saveAction(type: @UnsafeVariance K) {
        mutableList.add(executor.saveAction(type) as T)
    }

    fun <K: State> routeToState(state: @UnsafeVariance K)  {
        mutableList.add(executor.routeToState(state) as T)
    }

    fun build(): List<T> = mutableList.toList()
}
