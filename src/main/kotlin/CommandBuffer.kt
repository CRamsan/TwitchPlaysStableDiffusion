import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import kotlin.random.Random

private val logger = LoggerFactory.getLogger(CommandBuffer::class.java)

/**
 * This class can hold [Command] instances associated to a user.
 */
class CommandBuffer(
    private val random: Random,
) {
    private val _commandMap = mutableMapOf<String, SdCommand>()
    private val _commandMapState = MutableStateFlow<Map<String, SdCommand>>(_commandMap)
    val commandMapState = _commandMapState.asStateFlow()

    /**
     * Store a [command] and associate it with a [userName]. This function can be called from any thread.
     */
    suspend fun store(userName: String, command: SdCommand) = withContext(Dispatchers.Main) {
        logger.info("Storing for $userName - command $command")
        _commandMap[userName] = command
        _commandMapState.value = _commandMap.toMap()
    }

    /**
     * Clear the command queue and fetch a randomly selected command.
     */
    suspend fun flush(): SdCommand? = withContext(Dispatchers.Main) {
        val command = _commandMap.values.randomOrNull(random)
        logger.info("Flushing commands, got $command")
        _commandMap.clear()
        _commandMapState.value = _commandMap.toMap()
        command
    }
}
