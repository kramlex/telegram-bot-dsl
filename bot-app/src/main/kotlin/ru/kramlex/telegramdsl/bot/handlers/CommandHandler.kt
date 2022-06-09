package ru.kramlex.telegramdsl.bot.handlers

import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.asFromUser
import dev.inmo.tgbotapi.utils.PreviewFeature
import ru.kramlex.telegramdsl.bot.repositories.UserRepository
import ru.kramlex.telegramdsl.bot.utils.chatId

@PreviewFeature
suspend fun BehaviourContext.handleCommand(userRepository: UserRepository) {
    onCommand("start") { message ->
        println("[START] message: $message")
        val chatId = message.chatId
        val existUser = userRepository.getUser(chatId.chatId)
        if (existUser == null) {
            val user = message.asFromUser()?.user
                ?: throw java.lang.IllegalStateException("failed to get user information")
            userRepository.saveUser(user)
        } else {
            val name = existUser.nickName ?: existUser.fullName
            sendMessage(
                chatId = chatId,
                text = "Hello again, $name."
            )
        }
    }
    onCommand("drop") { message ->
        println("[DROP] message: $message")
        userRepository.dropData()
    }
}
