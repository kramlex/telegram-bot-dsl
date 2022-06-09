package ru.kramlex.telegramdsl.bot

import dev.inmo.tgbotapi.utils.PreviewFeature
import kotlinx.coroutines.runBlocking

@PreviewFeature
fun main() = runBlocking {
    val bot = DSLTGBot()
    bot.start()
}
