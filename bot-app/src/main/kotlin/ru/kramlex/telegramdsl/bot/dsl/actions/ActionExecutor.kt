package ru.kramlex.telegramdsl.bot.dsl.actions

import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage

abstract class ActionExecutor<out TypeState: State, out TypeSave: SaveType> {

    sealed interface ActionType: Action
    sealed interface WithoutMessage: Action

    interface ExecutableWithMessage: Action {
        fun execute(message: CommonMessage<*>)
    }

    interface ExecutableWithContext: WithoutMessage, Action {
        suspend fun executeWithContext(context: BehaviourContext, chatId: ChatId)
    }

    interface Executable: WithoutMessage {
        fun execute(id: Long)
    }

    fun sendTextMessage(message: String) = object : Action.SendMessage(message = message) {
        override suspend fun executeWithContext(context: BehaviourContext, chatId: ChatId) {
            val lambda: suspend BehaviourContext.() -> Unit = {
                sendMessage(
                    chatId = chatId,
                    text = message
                )
            }
            lambda.invoke(context)
        }
    }

    fun saveAction(type: @UnsafeVariance TypeSave) = object : Action.Save(type) {
        override fun execute(message: CommonMessage<*>) {
            saveData(type, message)
        }
    }

    fun routeToState(state: @UnsafeVariance TypeState) = object : Action.Route(state = state) {
        override fun execute(id: Long) {
            route(state, id)
        }
    }

    protected abstract fun route(state: @UnsafeVariance TypeState, id: Long)

    protected abstract fun saveData(type: @UnsafeVariance TypeSave, message: CommonMessage<*>)
}
