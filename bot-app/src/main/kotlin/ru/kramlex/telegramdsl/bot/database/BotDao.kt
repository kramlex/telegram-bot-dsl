package ru.kramlex.telegramdsl.bot.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.kramlex.db.generated.BotDatabase
import ru.kramlex.telegramdsl.bot.database.tables.UserDao

class BotDao(
    private val botDatabase: BotDatabase
) {

    private val coroutineScope: CoroutineScope =
        CoroutineScope(Dispatchers.IO)

    val userDao: UserDao by lazy {
        UserDao(
            userRowQueries = botDatabase.userRowQueries,
            parentScope = coroutineScope
        )
    }
}
