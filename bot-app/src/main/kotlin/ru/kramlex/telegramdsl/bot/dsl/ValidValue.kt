package ru.kramlex.telegramdsl.bot.dsl

sealed interface ValidValue {
    object Any : ValidValue

    data class Value(
        val string: String,
        val invalidError: String
    )

    data class Regex(
        val regex: String,
        val invalidError: String
    ) : ValidValue

    data class Enum(
        val values: List<String>,
        val invalidError: String
    ) : ValidValue

    fun validate(string: String): Boolean {
        return when (this) {
            Any -> true
            is Enum -> {
                if (this.values.contains(string)) true
                else throw InvalidValue(invalidError)
            }
            is Regex -> {
                if (regex.toRegex().containsMatchIn(string)) true
                else throw InvalidValue(invalidError)
            }
        }
    }

    class InvalidValue(val errorMessage: String) : Throwable(errorMessage)
}


class InvalidErrorContext(
    val invalidErrorText: String
) {

    fun value(string: String): ValidValue.Value =
        ValidValue.Value(string = string, invalidError = invalidErrorText)

    fun regex(regex: String) =
        ValidValue.Regex(regex = regex, invalidError = invalidErrorText)

    fun enum(values: List<String>) =
        ValidValue.Enum(values = values, invalidError = invalidErrorText)

}
