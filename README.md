# Telegram Bot with Kotlin DSL 

## Description

This is a demo project of [TelegramBotAPI](https://github.com/InsanusMokrassar/TelegramBotAPI) working with a DSL written in Kotlin.

![in_action](images/in_action.gif)

## Initialization
You need to describe all possible states, 
you can use **enum** or **sealed** classes, the main thing is that this object is [serializable](https://github.com/Kotlin/kotlinx.serialization).

```kotlin
@Serializable
sealed class UserState: State

sealed interface StartedState {
    @Serializable
    object HelloMessage : StartedState, UserState() {
        const val message: String = "Hello everyone, this bot is written using Kotlin DSL."
    }

    @Serializable
    object EnterOrganization : StartedState, UserState() {
        const val message: String = "Please specify the name of your organization: "
    }

    @Serializable
    object EnterName : StartedState, UserState() {
        const val message: String = "How can I contact you?"
    }
}

@Serializable
object Final : UserState() {
    const val message: String = "No more actions"
}
```

Next, we will need to create an enumeration of save types
for our bot. To do this, you need to use any object that implements the `SaveType` interface.

```kotlin
sealed interface UserSaveType: SaveType
object SaveOrganization : UserSaveType
object SaveName : UserSaveType
```

Let's move on to the implementation of the abstract `ActionExecutor` class. 
It is he who performs the processing of all our actions.

```kotlin
class AppActionExecutor(
    private val database: Database
): ActionExecutor<UserState, UserSaveType>() {

    override fun route(state: UserState, id: Long) {
        // route
    }

    override fun saveData(type: UserSaveType, message: CommonMessage<*>) {
        val chatId: Long = message.chat.id.chatId
        val messageText = message.text ?: return
        // save data
    }
}
```

This is a generalized abstract class that has 
parametrization by states and types of conservation.

## State manager

### Usage

```kotlin
class BotManager(
    private val database: Database,
) {
    private val actionExecutor = AppActionExecutor(userRepository)
    private val manager = StateManager<State, AppActionExecutor>(actionExecutor)

    fun getStateInfo(userId: Long): StateInfo<AppActionExecutor>? {
        val databaseUser = database.getUser(userId) ?: return null
        return getStateInfo(userState = userRow.state)
    }

    private fun getStateInfo(userState: UserState): StateInfo<AppActionExecutor>? {
        return manager.getState(userState)
    }
    
    // ...
}
```

He stores all the states in himself and
knows how to perform actions with them.

### Creating states

In order to describe possible states, we will 
use a special DSL written in Kotlin.

We will describe the states in the `init` block of our manager: 

```kotlin

// ...

init {
    manager.states {
        // HelloMessage
        addState(StartedState.HelloMessage) {
            startActions {
                addActions {
                    sendTextMessage(StartedState.HelloMessage.message)
                    routeToState(StartedState.EnterOrganization)
                }
            }
        }

        // EnterOrganization
        addState(StartedState.EnterOrganization) {
            startActions {
                addAction { sendTextMessage(StartedState.EnterOrganization.message) }
            }
            anyValues {
                addAction { saveAction(SaveOrganization) }
                addAction { routeToState(StartedState.EnterName) }
            }
        }

        // EnterName
        addState(StartedState.EnterName) {
            startActions {
                addAction { sendTextMessage(StartedState.EnterName.message) }
            }
            anyValues {
                addActions {
                    saveAction(SaveName)
                    routeToState(Final)
                }
            }
        }

        // Final
        addState(Final) {
            startActions {
                addAction { sendTextMessage(Final.message) }
            }
        }
    }
}

// ...

```

## Usage

To work with this manager, we will create several handlers for BehaviorContext:

### Command Handler

```kotlin
suspend fun BehaviourContext.handleCommand(userRepository: UserRepository) {
    onCommand("start") { message ->
        println("[START] message: $message")
        val chatId = message.chatId
        val existUser = userRepository.getUser(chatId.chatId)
        if (existUser == null) {
            val user = message.asFromUser()?.user
                ?: throw java.lang.IllegalStateException("failed to get user information")
            userRepository.saveUser(user)
        } else {
            val name = existUser.nickName ?: existUser.fullName
            sendMessage(
                chatId = chatId,
                text = "Hello again, $name."
            )
        }
    }
    onCommand("drop") { message ->
        println("[DROP] message: $message")
        userRepository.dropData()
    }
}
```

### State Handler

```kotlin
suspend fun BehaviourContext.handleState(
    user: DatabaseUser,
    botManager: BotManager
) {
    val stateInfo = botManager.getStateInfo(user.id) ?: return

    stateInfo.enterStateActions
        .forEach { action ->
            when (action) {
                is ActionExecutor.ExecutableWithContext -> action.executeWithContext(this, user.id.chatId)
                is ActionExecutor.Executable -> action.execute(user.id)
            }
        }
}
```

### Text Handler

```kotlin
@OptIn(PreviewFeature::class)
suspend fun BehaviourContext.handleTextWithoutCommands(
    manager: BotManager,
) {
    onText { message ->
        if (message.isCommand) return@onText
        println("[TEXT] message: $message")
        processTextInput(
            message = message,
            manager = manager
        )
    }
}

@PreviewFeature
suspend fun BehaviourContext.processTextInput(
    manager: BotManager,
    message: CommonMessage<TextContent>
) {

    suspend fun executeAction(action: Action) {
        val chatId = message.chatId
        when (action) {
            is Action.Route -> action.execute(message.chatId.chatId)
            is Action.SendMessage -> action.executeWithContext(
                context = this, chatId = chatId
            )
            is Action.Save -> action.execute(message)
        }
    }

    val userId = message.chatId.chatId
    val userState = manager.getStateInfo(userId)
    if (userState == null) {
        println("An unauthorized user has sent a message!")
        return
    }

    val messageText = message.text

    val values = userState.values
    check(values != null) { return }

    val errorAction = userState.values.validate(messageText)
    if (errorAction != null) {
        executeAction(errorAction)
    } else {
        when (values) {
            is Values.Any -> values.actions.forEach {
                executeAction(it)
            }
            is Values.Constant -> values.actions.forEach {
                executeAction(it)
            }
            is Values.Many -> values.getAction(messageText)
            is Values.Regex -> values.actions.forEach {
                executeAction(it)
            }
        }
    }
}
```

## Usage with [TelegramBotAPI](https://github.com/InsanusMokrassar/TelegramBotAPI)

It is enough to run these handlers in the `buildBehaviourWithLongPolling` scope.

```kotlin

// ...

private val database: Database by lazy {
    Database()
}

// Management
private val botManager: BotManager by lazy {
    BotManager(database)
}

// start func
suspend fun start() {
    bot.buildBehaviourWithLongPolling {

        println(getMe())

        userRepository.changes.receiveAsFlow()
            .onEach { handleState(it, botManager) }
            .launchIn(scope)

        handleCommand(userRepository)
        handleTextWithoutCommands(
            manager = botManager
        )

    }.join()
}

// ... 

```

## Custom Actions


### Create 

Let's create an enumeration of user actions, the main thing is 
that they implement the necessary abstract class.

```kotlin
sealed interface UserAction {
    abstract class HelloUser : UserAction, ActionExecutor.ExecutableWithContext
}
```

### Action Implementation


It is necessary to create an implementation of the 
previously created action inside the `AppActionExecutor`.

```kotlin

// ...

// extend the standard executor
fun helloMessage() = object : ExecutableWithContext {
    override suspend fun executeWithContext(context: BehaviourContext, chatId: ChatId) {
        val user = userRepository.getUser(chatId.chatId) ?: return
        sendTextMessage("Hello,  ${user.nickName ?: user.fullName}")
            .executeWithContext(context, chatId)
    }
}

// ...

```

### Usage

Now you can use this action in DSL.

```kotlin
addState(Final) {
    startActions {
        addAction { helloMessage() } // adition action
        addAction { sendTextMessage(Final.message) }
    }
}
```
