@file:OptIn(DelicateCoroutinesApi::class)

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import sd.StableDiffusionClient
import ui.pageGUI
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds

private val logger: Logger by lazy { LoggerFactory.getLogger("Main") }

private val serializer = Json {
    ignoreUnknownKeys = true
    isLenient = true
    prettyPrint = false
}

private var listenToChat = true
private var bufferTime =  10
private var idleStartTime = LocalDateTime.now()
private var queueJob: Job? = null

private val bufferCounter = MutableStateFlow(0)
private val inIdleMode = MutableStateFlow(false)

lateinit var sdClient: StableDiffusionClient
lateinit var commandBuffer: CommandBuffer
lateinit var idleManager: IdleManager

const val ENV_ACCESS_TOKEN = "ACCESS_TOKEN"
const val ENV_CHANNEL_NAME = "CHANNEL_NAME"

var channelName = ""

fun main() {
    logger.info("Launching program")

    val client = HttpClient(Java) {
        engine {
        }
        install(ContentNegotiation) {
            json(serializer)
        }
    }

    val accessToken = System.getenv(ENV_ACCESS_TOKEN) ?: throw RuntimeException("Set the $ENV_ACCESS_TOKEN env variable")
    channelName = System.getenv(ENV_CHANNEL_NAME) ?: throw RuntimeException("Set the $ENV_CHANNEL_NAME env variable")

    // chat credential
    val credential = OAuth2Credential("twitch", accessToken)
    val twitchClient = TwitchClientBuilder.builder()
        .withEnableHelix(true)
        .withEnableChat(true)
        .withChatAccount(credential)
        .build()

    sdClient = StableDiffusionClient(client, serializer)
    commandBuffer = CommandBuffer(Random.Default)

    idleManager = IdleManager()

    observeChat(twitchClient)
    observeConsoleInput()
    processCommandQueueAsync()

    twitchClient.chat.joinChannel(channelName)

    pageGUI(
        sdClient,
        bufferCounter,
        commandBuffer.commandMapState,
        inIdleMode,
    )
}

@OptIn(DelicateCoroutinesApi::class)
fun processCommandQueueAsync() {
    queueJob?.cancel()
    queueJob = GlobalScope.launch {
        logger.info("Command Queue was started")
        while (true) {
            try {
                bufferCounter.value = bufferTime
                repeat(bufferTime) {
                    delay(1.seconds)
                    bufferCounter.value--
                }
                logger.debug("Fetching command")
                val command = commandBuffer.flush()
                if (command != null) {
                    idleStartTime = LocalDateTime.now()
                    bufferCounter.value--
                    sdClient.handleCommands(command)
                }
                inIdleMode.value = idleStartTime.isBefore(LocalDateTime.now().minusMinutes(5))
                if (inIdleMode.value) {
                    bufferCounter.value = -1
                    runIdleCommand()
                }
            } catch (cancellation: CancellationException) {
                logger.info("Command Queue was stopped")
                break
            } catch (throwable: Throwable) {
                logger.error("Exception: ${throwable.message}")
                throwable.consoleOut()
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun observeConsoleInput() {
    GlobalScope.launch(Dispatchers.IO) {
        while (true) {
            try {
                val line = readln()
                logger.info("Console In: $line")
                val command = parseCommand(line).getOrThrow()

                if (command is AdminCommand) {
                    when (command) {
                        is AdminCommand.ListenToChat -> updateListenToChat(command)
                        is AdminCommand.SetBufferTime -> updateBufferTime(command)
                        AdminCommand.RunIdleCommand -> runIdleCommand()
                        AdminCommand.RunCommandQueue -> processCommandQueueAsync()
                        AdminCommand.StopCommandQueue -> stopCommandQueue()
                        is AdminCommand.SetIdleMode -> setIdleMode(command.idleMode)
                    }
                } else if (command is SdCommand) {
                    sdClient.handleCommands(command)
                } else {
                    logger.warn("Could not map console command")
                }
            } catch (throwable: Throwable) {
                logger.error("Exception: ${throwable.message}")
                throwable.consoleOut()
            }
        }
    }
}

private fun Throwable.consoleOut() {
    println(message)
    printStackTrace()
}
fun setIdleMode(idleMode: Boolean) {
    inIdleMode.value = idleMode
    idleStartTime = if (idleMode) {
        LocalDateTime.MIN
    } else {
        LocalDateTime.now()
    }
}

fun stopCommandQueue() {
    queueJob?.cancel()
}

suspend fun runIdleCommand() {
    logger.info("Executing idle command")
    val idleCommand = idleManager.getNewCommand(sdClient.uiState.value.positivePrompt.size)

    if (idleCommand != null) {
        sdClient.handleCommands(idleCommand)
    }
}

fun updateListenToChat(adminCommand: AdminCommand.ListenToChat) {
    logger.info("Setting listenToChat=${adminCommand.listenToChat}")
    listenToChat = adminCommand.listenToChat
}

fun updateBufferTime(adminCommand: AdminCommand.SetBufferTime) {
    logger.info("Setting bufferTime=${adminCommand.duration}")
    bufferTime = (adminCommand.duration.inWholeSeconds).toInt()
}

fun observeChat(twitchClient: TwitchClient) {
    twitchClient.eventManager.onEvent(ChannelMessageEvent::class.java) { event ->
        val username = event.user.name
        try {
            val message = event.message
            logger.info("channel: ${event.channel.name}, username:$username, message:$message")
            if (event.message.equals("Execute Order 66.", ignoreCase = true)) {
                exitProcess(-2)
            }
            if (!listenToChat) {
                return@onEvent
            }
            val command = parseCommand(event.message).getOrThrow() ?: return@onEvent

            if (command is AdminCommand) {
                logger.warn("Admin commands cannot be run from chat")
            } else if (command is SdCommand) {
                val username = event.user.name
                GlobalScope.launch(Dispatchers.Main) {
                    commandBuffer.store(username, command)
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            logger.error("Exception: ${throwable.message}")
            throwable.consoleOut()
            if (!twitchClient.chat.sendMessage(
                channelName,
                "Hi $username, your message could not be processed. The reason was: \"${throwable.message}\"",
            )) {
                logger.error("Could not send reply back to user $username.")
            } else {
                logger.info("Replied back to user $username.")
            }
        }
    }
}