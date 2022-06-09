package ru.kramlex.telegramdsl.bot.database.adapters

import app.cash.sqldelight.ColumnAdapter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.kramlex.telegramdsl.bot.states.data.UserState

class UserStateAdapter(
    private val json: Json
): ColumnAdapter<UserState, String> {

    override fun decode(databaseValue: String): UserState =
        json.decodeFromString(databaseValue)

    override fun encode(value: UserState): String =
        json.encodeToString(value)
}
