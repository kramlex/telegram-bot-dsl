package ru.kramlex.telegramdsl.bot.states.data

import kotlinx.serialization.Serializable
import ru.kramlex.telegramdsl.bot.dsl.actions.State

@Serializable
sealed class UserState: State

sealed interface StartedState {
    @Serializable
    object HelloMessage : StartedState, UserState() {
        const val message: String = "Hello everyone, this bot is written using Kotlin DSL."
    }

    @Serializable
    object EnterOrganization : StartedState, UserState() {
        const val message: String = "Please specify the name of your organization: "
    }

    @Serializable
    object EnterName : StartedState, UserState() {
        const val message: String = "How can I contact you?"
    }
}

@Serializable
object Final : UserState() {
    const val message: String = "No more actions"
}
