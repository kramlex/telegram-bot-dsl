package ru.kramlex.telegramdsl.bot.states.data

import ru.kramlex.telegramdsl.bot.dsl.actions.ActionExecutor

sealed interface UserAction {
    abstract class HelloUser : UserAction, ActionExecutor.ExecutableWithContext
}
