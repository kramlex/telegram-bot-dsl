package ru.kramlex.telegramdsl.bot.utils

import dev.inmo.tgbotapi.types.chat.User

val User.idLong: Long get() = this.id.chatId
