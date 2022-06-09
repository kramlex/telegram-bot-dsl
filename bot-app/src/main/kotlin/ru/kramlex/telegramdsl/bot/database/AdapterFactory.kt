package ru.kramlex.telegramdsl.bot.database

import kotlinx.serialization.json.Json
import ru.kramlex.db.generated.UserRow
import ru.kramlex.telegramdsl.bot.database.adapters.UserStateAdapter

internal class AdapterFactory(
    private val json: Json
) {
    val userAdapter: UserRow.Adapter
        get() = UserRow.Adapter(
            stateAdapter = UserStateAdapter(json)
        )
}
