package ru.kramlex.telegramdsl.bot.states.data

import ru.kramlex.telegramdsl.bot.dsl.actions.SaveType


sealed interface UserSaveType: SaveType
object SaveOrganization : UserSaveType
object SaveName : UserSaveType
