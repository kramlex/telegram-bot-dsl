package ru.kramlex.telegramdsl.bot.repositories

import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.kramlex.db.generated.UserRow
import ru.kramlex.telegramdsl.bot.database.tables.UserDao
import ru.kramlex.telegramdsl.bot.states.data.StartedState
import ru.kramlex.telegramdsl.bot.utils.fullname
import ru.kramlex.telegramdsl.bot.utils.idLong

@PreviewFeature
class UserRepository(
    private val userDao: UserDao
) {
    private val coroutineScope =
        CoroutineScope(Dispatchers.Default)

    private val _stateUsers: MutableStateFlow<List<UserRow>> =
        MutableStateFlow(emptyList())

    val changes: Channel<UserRow> = Channel(CHANNEL_SIZE)

    fun getUser(userId: Long): UserRow? =
        userDao.getUser(userId)

    init {
        _stateUsers.value = userDao.getAllUsers()
        userDao.getAllUsersFlow()
            .onEach { allUsers ->
                val oldUsers = _stateUsers.value

                val usersWithChanges = allUsers.filter { user ->
                    val existUser = oldUsers.firstOrNull { it.id == user.id }
                        ?: return@filter true
                    existUser.state != user.state
                }
                usersWithChanges.forEach {
                    changes.trySend(it)
                }

                _stateUsers.value = allUsers
            }.launchIn(coroutineScope)
    }

    fun saveUser(user: User) {
        userDao.upsertUser(
            id = user.idLong,
            fullName = user.fullname,
            state = StartedState.HelloMessage
        )
    }

    fun dropData() = userDao.dropTable()

    fun saveData(id: Long, block: UserDao.(Long) -> Unit) = userDao.block(id)

    companion object {
        const val CHANNEL_SIZE = 128
    }
}
