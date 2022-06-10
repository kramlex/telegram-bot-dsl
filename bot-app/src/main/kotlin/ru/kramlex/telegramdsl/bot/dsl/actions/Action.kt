package ru.kramlex.telegramdsl.bot.dsl.actions

import ru.kramlex.telegramdsl.bot.dsl.StateDslMarker


sealed interface Action {
    abstract class Save(saveType: SaveType) : ActionExecutor.ExecutableWithMessage
    abstract class SendMessage(val message: String) : ActionExecutor.ExecutableWithContext
    abstract class Route(val state: State) : Action, ActionExecutor.Executable
}
