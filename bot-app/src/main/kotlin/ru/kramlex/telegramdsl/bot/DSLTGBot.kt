package ru.kramlex.telegramdsl.bot

import app.cash.sqldelight.db.SqlDriver
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.Json
import ru.kramlex.db.generated.BotDatabase
import ru.kramlex.telegramdsl.bot.database.AdapterFactory
import ru.kramlex.telegramdsl.bot.database.BotDao
import ru.kramlex.telegramdsl.bot.database.JvmSqliteDriver
import ru.kramlex.telegramdsl.bot.handlers.handleCommand
import ru.kramlex.telegramdsl.bot.handlers.handleState
import ru.kramlex.telegramdsl.bot.handlers.handleTextWithoutCommands
import ru.kramlex.telegramdsl.bot.model.Constants
import ru.kramlex.telegramdsl.bot.repositories.UserRepository
import ru.kramlex.telegramdsl.bot.states.BotManager

@PreviewFeature
internal class DSLTGBot {

    private val bot = telegramBot(Constants.BOT_API_KEY)

    // Json
    private val json by lazy { Json { ignoreUnknownKeys = false } }

    // Database
    private val adapterFactory by lazy { AdapterFactory(json) }
    private val botDao by lazy {
        val driver: SqlDriver = JvmSqliteDriver(
            schema = BotDatabase.Schema,
            path = Constants.DATABASE_NAME
        )
        BotDao(
            BotDatabase(
                driver = driver,
                UserRowAdapter = adapterFactory.userAdapter
            )
        )
    }

    // Repositories
    private val userRepository by lazy {
        UserRepository(
            userDao = botDao.userDao
        )
    }

    // Management
    private val botManager: BotManager by lazy {
        BotManager(userRepository)
    }

    // start func
    suspend fun start() {
        bot.buildBehaviourWithLongPolling {

            println(getMe())

            userRepository.changes.receiveAsFlow()
                .onEach { handleState(it, botManager) }
                .launchIn(scope)

            handleCommand(userRepository)
            handleTextWithoutCommands(
                manager = botManager
            )

        }.join()
    }
}
