package ru.kramlex.telegramdsl.bot.states.data

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.utils.PreviewFeature
import dev.inmo.tgbotapi.utils.RiskFeature
import ru.kramlex.telegramdsl.bot.dsl.actions.ActionExecutor
import ru.kramlex.telegramdsl.bot.repositories.UserRepository

@RiskFeature
@PreviewFeature
class AppActionExecutor(
    private val userRepository: UserRepository
): ActionExecutor<UserState, UserSaveType>() {

    override fun route(state: UserState, id: Long) {
        userRepository.saveData(id) { updateUserState(it, state)}
    }

    override fun saveData(type: UserSaveType, message: CommonMessage<*>) {
        val chatId: Long = message.chat.id.chatId
        val messageText = message.text ?: return
        userRepository.saveData(chatId) {
            when (type) {
                SaveName -> updateNickname(it, messageText)
                SaveOrganization -> updateOrganization(it, messageText)
            }
        }
    }

    // extend the standard executor
    fun helloMessage() = object : ExecutableWithContext {
        override suspend fun executeWithContext(context: BehaviourContext, chatId: ChatId) {
            val user = userRepository.getUser(chatId.chatId) ?: return
            sendTextMessage("Hello,  ${user.nickName ?: user.fullName}")
                .executeWithContext(context, chatId)
        }
    }
}
