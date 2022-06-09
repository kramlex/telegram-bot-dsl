package ru.kramlex.telegramdsl.bot.states

import dev.inmo.tgbotapi.utils.PreviewFeature
import dev.inmo.tgbotapi.utils.RiskFeature
import ru.kramlex.telegramdsl.bot.dsl.StateInfo
import ru.kramlex.telegramdsl.bot.dsl.StateManager
import ru.kramlex.telegramdsl.bot.dsl.actions.State
import ru.kramlex.telegramdsl.bot.repositories.UserRepository
import ru.kramlex.telegramdsl.bot.states.data.*

@OptIn(PreviewFeature::class, RiskFeature::class)
class BotManager(
    private val userRepository: UserRepository,
) {
    private val actionExecutor = AppActionExecutor(userRepository)
    private val manager = StateManager<State, AppActionExecutor>(actionExecutor)

    fun getStateInfo(userId: Long): StateInfo<AppActionExecutor>? {
        val userRow = userRepository.getUser(userId) ?: return null
        return getStateInfo(userState = userRow.state)
    }

    private fun getStateInfo(userState: UserState): StateInfo<AppActionExecutor>? {
        return manager.getState(userState)
    }

    init {
        manager.states {
            // HelloMessage
            addState(StartedState.HelloMessage) {
                startActions {
                    addActions {
                        sendTextMessage(StartedState.HelloMessage.message)
                        routeToState(StartedState.EnterOrganization)
                    }
                }
            }

            // EnterOrganization
            addState(StartedState.EnterOrganization) {
                startActions {
                    addAction { sendTextMessage(StartedState.EnterOrganization.message) }
                }
                anyValues {
                    addAction { saveAction(SaveOrganization) }
                    addAction { routeToState(StartedState.EnterName) }
                }
            }

            // EnterName
            addState(StartedState.EnterName) {
                startActions {
                    addAction { sendTextMessage(StartedState.EnterName.message) }
                }
                anyValues {
                    addActions {
                        saveAction(SaveName)
                        routeToState(Final)
                    }
                }
            }

            // Final
            addState(Final) {
                startActions {
                    addAction { helloMessage() } // adition action
                    addAction { sendTextMessage(Final.message) }
                }
            }
        }
    }
}
