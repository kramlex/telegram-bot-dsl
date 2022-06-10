package ru.kramlex.telegramdsl.bot.dsl

import ru.kramlex.telegramdsl.bot.dsl.actions.Action
import ru.kramlex.telegramdsl.bot.dsl.actions.ActionExecutor
import ru.kramlex.telegramdsl.bot.dsl.actions.SaveType
import ru.kramlex.telegramdsl.bot.dsl.actions.State

data class StateInfo<out E : ActionExecutor<State, SaveType>>(
    val enterStateActions: List<ActionExecutor.WithoutMessage> = emptyList(),
    val menu: String? = null,
    val values: Values<E>? = null
) {

    @StateDslMarker
    class Builder<out E : ActionExecutor<State, SaveType>>(
        private val executor: E
    ) {
        private var enterStateActions: List<ActionExecutor.WithoutMessage> = emptyList()
        private var menu: String? = null
        private var values: Values<E>? = null

        @StateDslMarker
        fun startActions(lambda: LineListBuilder<ActionExecutor.WithoutMessage, E>.() -> Unit) {
            enterStateActions = LineListBuilder<ActionExecutor.WithoutMessage, E>(executor)
                .apply(lambda)
                .build()
        }

        @StateDslMarker
        fun menu(menu: String?) {
            this.menu = menu
        }


        @StateDslMarker
        fun anyValues(lambda: LineListBuilder<Action, E>.() -> Unit) {
            this.values = Values.Any(
                executor = executor,
                lambda = lambda
            )
        }

        @StateDslMarker
        fun manyValues(
            errorAction: Action.SendMessage,
            invalidErrorText: String,
            lambda: MapListBuilder<Action, E>.() -> Unit
        ) {
            this.values = Values.Many(
                executor = executor,
                errorAction = executor.sendTextMessage(invalidErrorText),
                lambda = lambda
            )
        }

        @StateDslMarker
        fun regexValues(
            regex: String,
            invalidErrorText: String,
            lambda: LineListBuilder<Action, E>.() -> Unit
        ) {
            this.values = Values.Regex(
                regex = regex,
                executor = executor,
                errorAction = executor.sendTextMessage(invalidErrorText),
                lambda = lambda
            )
        }

        @StateDslMarker
        fun constantValue(
            string: String,
            invalidErrorText: String,
            lambda: LineListBuilder<Action, E>.() -> Unit
        ) {
            this.values = Values.Constant(
                string = string,
                executor = executor,
                errorAction = executor.sendTextMessage(invalidErrorText),
                lambda = lambda
            )
        }

        @StateDslMarker
        fun InvalidErrorContext.manyValues(
            lambda: MapListBuilder<Action, E>.() -> Unit
        ) {
            this@Builder.values = Values.Many(
                executor = executor,
                errorAction = executor.sendTextMessage(invalidErrorText),
                lambda = lambda
            )
        }

        @StateDslMarker
        fun InvalidErrorContext.regexValues(
            regex: String,
            lambda: LineListBuilder<Action, E>.() -> Unit
        ) {
            this@Builder.values = Values.Regex(
                regex = regex,
                executor = executor,
                errorAction = executor.sendTextMessage(invalidErrorText),
                lambda = lambda
            )
        }

        @StateDslMarker
        fun InvalidErrorContext.constantValue(
            string: String,
            lambda: LineListBuilder<Action, E>.() -> Unit
        ) {
            this@Builder.values = Values.Constant(
                string = string,
                executor = executor,
                errorAction = executor.sendTextMessage(invalidErrorText),
                lambda = lambda
            )
        }

        @StateDslMarker
        operator fun String.invoke(lambda: InvalidErrorContext.() -> Unit) {
            InvalidErrorContext(this).lambda()
        }

        fun build(): StateInfo<E> = StateInfo(
            enterStateActions = enterStateActions,
            menu = menu,
            values = values
        )
    }
}
