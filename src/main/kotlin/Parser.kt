import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Take in an [input] and parse it to provide a [Command]. The return value is a [Result], so the caller needs to
 * verify that we were able to get a valid [Command].
 */
fun parseCommand(input: String): Result<Command?> = runCatching {
    val tokens = input.trim().split(" ")

    try {
        val rawPrefix = tokens.firstOrNull()?.uppercase() ?: return@runCatching null

        val prefix = Prefix.values().find { it.name == rawPrefix } ?: return@runCatching null

        if (input.containsInvalidCharacters()) {
            throw RuntimeException("Input contains invalid characters.")
        }

        if (input.isTooLong()) {
            throw RuntimeException("Input is too long. Max length is $MAX_LENGTH.")
        }

        when (prefix) {
            Prefix.SD -> parseSdCommand(tokens.subList(1, tokens.size))
            Prefix.SDA -> parseAdminCommand(tokens.subList(1, tokens.size))
        }
    } catch (enumFailure: IllegalArgumentException) {
        throw RuntimeException("Not a valid command.", enumFailure)
    }
}

fun parseAdminCommand(tokens: List<String>): AdminCommand {
    val category = AdminCategory.valueOf(tokens.first().uppercase())
    val param = tokens.getOrNull(1)

    return when (category) {
        AdminCategory.BUFFER_TIME -> {
            var bufferTime = (param?.toIntOrNull() ?: 10).seconds
            bufferTime = if (bufferTime < 5.seconds || bufferTime > 5.minutes) {
                10.seconds
            } else {
                bufferTime
            }

            AdminCommand.SetBufferTime(bufferTime)
        }
        AdminCategory.LISTEN_TO_CHAT -> {
            val newSetting = param?.toBooleanStrictOrNull() ?: false
            AdminCommand.ListenToChat(newSetting)
        }
        AdminCategory.IDLE_COMMAND -> { AdminCommand.RunIdleCommand }
        AdminCategory.RUN_COMMAND_QUEUE -> { AdminCommand.RunCommandQueue }
        AdminCategory.STOP_COMMAND_QUEUE -> { AdminCommand.StopCommandQueue }
        AdminCategory.SET_IDLE_MODE -> { AdminCommand.SetIdleMode(
            param?.toBoolean() ?: false
        ) }
    }
}

fun parseSdCommand(tokens: List<String>): SdCommand {
    val category = SdCategory.valueOf(tokens.first().uppercase())
    val action by lazy { Action.valueOf(tokens[1].uppercase()) }
    val index by lazy { tokens[2].toInt() }

    return when (category) {
        SdCategory.CFG -> when (action) {
            Action.INCREMENT -> SdCommand.IncrementCFGScale
            Action.DECREMENT -> SdCommand.DecrementCFGScale
            else -> TODO()
        }
        SdCategory.HEIGHT -> when (action) {
            Action.INCREMENT -> SdCommand.IncrementHeight()
            Action.DECREMENT -> SdCommand.DecrementHeight()
            else -> TODO()
        }
        SdCategory.WIDTH -> when (action) {
            Action.INCREMENT -> SdCommand.IncrementWidth()
            Action.DECREMENT -> SdCommand.DecrementWidth()
            else -> TODO()
        }
        SdCategory.SEED -> SdCommand.NewSeed
        SdCategory.STEPS -> when (action) {
            Action.INCREMENT -> SdCommand.IncrementSamplingSteps
            Action.DECREMENT -> SdCommand.DecrementSamplingSteps
            else -> TODO()
        }
        SdCategory.PROMPT -> when (action) {
            Action.ADD -> SdCommand.AddTerm(tokens.getTerm(), index)
            Action.UPDATE -> SdCommand.UpdateTerm(tokens.getTerm(), index)
            Action.REMOVE -> SdCommand.RemoveTerm(index)
            Action.INCREMENT -> SdCommand.EmphasizeTerm(index)
            Action.DECREMENT -> SdCommand.DeemphasizeTerm(index)
        }
        SdCategory.NPROMPT -> when (action) {
            Action.ADD -> SdCommand.AddNegativeTerm(tokens.getTerm(), index)
            Action.UPDATE -> SdCommand.UpdateTerm(tokens.getTerm(), index)
            Action.REMOVE -> SdCommand.RemoveNegativeTerm(index)
            Action.INCREMENT -> SdCommand.EmphasizeNegativeTerm(index)
            Action.DECREMENT -> SdCommand.DeemphasizeNegativeTerm(index)
        }
    }
}

private fun List<String>.getTerm(): String {
    return if (size >= 4) {
        subList(3, size).joinToString(" ").let {
            if (it.isLetters()) {
                it
            } else {
                throw RuntimeException("The term should only contain alphanumeric characters.")
            }
        }
    } else {
        throw RuntimeException("The command is missing a term to add.")
    }
}

private fun String.containsInvalidCharacters() =
    contains('[') ||
    contains(']') ||
    contains('(') ||
    contains(')') ||
    contains('|')

const val MAX_LENGTH = 45
private fun String.isTooLong() = length > MAX_LENGTH

private fun String.isLetters(): Boolean {
    for (c in this)
    {
        if (c !in UPPER_CASE_LETTERS && c !in LOWER_CASE_LETTERS && c !in SYMBOLS && c !in NUMBERS) {
            return false
        }
    }
    return true
}

private val UPPER_CASE_LETTERS = 'A'..'Z'
private val LOWER_CASE_LETTERS = 'a'..'z'
private val NUMBERS = "0123456789".toCharArray()
private val SYMBOLS = " ,".toCharArray()

private enum class Prefix {
    SD,
    SDA,
}

private enum class SdCategory {
    CFG,
    HEIGHT,
    WIDTH,
    SEED,
    STEPS,
    PROMPT,
    NPROMPT,
}

private enum class AdminCategory {
    BUFFER_TIME,
    LISTEN_TO_CHAT,
    IDLE_COMMAND,
    RUN_COMMAND_QUEUE,
    STOP_COMMAND_QUEUE,
    SET_IDLE_MODE,
}

private enum class Action {
    ADD,
    REMOVE,
    UPDATE,
    INCREMENT,
    DECREMENT,
}