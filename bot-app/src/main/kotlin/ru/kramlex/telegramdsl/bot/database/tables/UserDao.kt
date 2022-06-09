package ru.kramlex.telegramdsl.bot.database.tables

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import ru.kramlex.db.generated.UserRow
import ru.kramlex.db.generated.UserRowQueries
import ru.kramlex.telegramdsl.bot.states.data.UserState

class UserDao(
    private val userRowQueries: UserRowQueries,
    private val parentScope: CoroutineScope
) {

    fun upsertUser(
        id: Long,
        fullName: String,
        state: UserState
    ) = userRowQueries.upsert(
        id = id,
        fullName = fullName,
        state = state
    )

    fun updateUserState(
        id: Long,
        newState: UserState
    ) = userRowQueries.updateUserState(
        state = newState,
        id = id
    )

    fun getAllUsersFlow(): Flow<List<UserRow>> =
        userRowQueries.getAllUsers()
            .asFlow()
            .mapToList(context = parentScope.coroutineContext)

    fun getAllUsers(): List<UserRow> =
        userRowQueries.getAllUsers()
            .executeAsList()

    fun getUser(id: Long): UserRow? =
        userRowQueries.getUserById(id = id)
            .executeAsOneOrNull()

    fun updateNickname(id: Long, nickname: String) =
        userRowQueries.updateNickname(
            id = id,
            nickName = nickname
        )

    fun updateOrganization(id: Long, organization: String) =
        userRowQueries.updateOrganization(
            id = id,
            organization = organization
        )

    fun dropTable() =
        userRowQueries.dropTable()
}