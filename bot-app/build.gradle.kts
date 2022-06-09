plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("app.cash.sqldelight")
}

group = "ru.kramlex"
version = "0.0.1"

application {
    mainClass.set("ru.kramlex.telegramdsl.bot.ApplicationKt")
    val isDevelopment: Boolean = project.ext.has("development")
}

dependencies {
    implementation(libs.coroutines)
    implementation(libs.tgBotApi)
    implementation(libs.sqlDelightCoroutines)
    implementation(libs.sqlDelightDriver)
}

sqldelight {
    database("BotDatabase") {
        packageName = "ru.kramlex.db.generated"
        sourceFolders = listOf("sqldelight")
        version = 1
    }
}