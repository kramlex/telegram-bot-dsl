package ru.kramlex.telegramdsl.bot.handlers

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.PreviewFeature
import ru.kramlex.telegramdsl.bot.dsl.Values
import ru.kramlex.telegramdsl.bot.dsl.actions.Action
import ru.kramlex.telegramdsl.bot.states.BotManager
import ru.kramlex.telegramdsl.bot.utils.chatId
import ru.kramlex.telegramdsl.bot.utils.isCommand
import ru.kramlex.telegramdsl.bot.utils.text

@OptIn(PreviewFeature::class)
suspend fun BehaviourContext.handleTextWithoutCommands(
    manager: BotManager,
) {
    onText { message ->
        if (message.isCommand) return@onText
        println("[TEXT] message: $message")
        processTextInput(
            message = message,
            manager = manager
        )
    }
}

@PreviewFeature
suspend fun BehaviourContext.processTextInput(
    manager: BotManager,
    message: CommonMessage<TextContent>
) {

    suspend fun executeAction(action: Action) {
        val chatId = message.chatId
        when (action) {
            is Action.Route -> action.execute(message.chatId.chatId)
            is Action.SendMessage -> action.executeWithContext(
                context = this, chatId = chatId
            )
            is Action.Save -> action.execute(message)
        }
    }

    val userId = message.chatId.chatId
    val userState = manager.getStateInfo(userId)
    if (userState == null) {
        println("An unauthorized user has sent a message!")
        return
    }

    val messageText = message.text

    val values = userState.values
    check(values != null) { return }

    val errorAction = userState.values.validate(messageText)
    if (errorAction != null) {
        executeAction(errorAction)
    } else {
        when (values) {
            is Values.Any -> values.actions.forEach {
                executeAction(it)
            }
            is Values.Constant -> values.actions.forEach {
                executeAction(it)
            }
            is Values.Many -> values.getAction(messageText)
            is Values.Regex -> values.actions.forEach {
                executeAction(it)
            }
        }
    }
}

