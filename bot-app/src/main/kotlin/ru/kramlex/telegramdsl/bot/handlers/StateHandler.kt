package ru.kramlex.telegramdsl.bot.handlers

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import ru.kramlex.db.generated.UserRow
import ru.kramlex.telegramdsl.bot.dsl.actions.Action
import ru.kramlex.telegramdsl.bot.dsl.actions.ActionExecutor
import ru.kramlex.telegramdsl.bot.states.BotManager
import ru.kramlex.telegramdsl.bot.utils.chatId

suspend fun BehaviourContext.handleState(
    user: UserRow,
    botManager: BotManager
) {
    val stateInfo = botManager.getStateInfo(user.id) ?: return

    stateInfo.enterStateActions
        .forEach { action ->
            when (action) {
                is ActionExecutor.ExecutableWithContext -> action.executeWithContext(this, user.id.chatId)
                is ActionExecutor.Executable -> action.execute(user.id)
            }
        }
}

