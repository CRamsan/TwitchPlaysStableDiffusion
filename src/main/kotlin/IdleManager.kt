import org.slf4j.LoggerFactory
import java.io.File
import kotlin.random.Random

/**
 * This class manages to retrieve [Command] to be used during idle times.
 */
class IdleManager {

    private val fileName = "prompt_terms.txt"

    /**
     * The [termCount] is the number of terms in the positive prompt. This is needed as the randomly selected
     * command could be to remove a term, in which case we need to ensure we provide a valid index.
     */
    fun getNewCommand(termCount: Int): SdCommand? {
        val terms = File(fileName).readLines()
        val term = terms.random().trim()

        if (term.isBlank()) {
            return null
        }

        val index = if (termCount > 0)
            Random.nextInt(termCount)
        else
            0

        val command = if (termCount < 10) {
            SdCommand.AddTerm(term, index)
        } else if (termCount > 25) {
            SdCommand.RemoveTerm(index)
        } else {
            val randomValue = Random.nextFloat()
            if (randomValue < 0.5) {
                SdCommand.AddTerm(term, index)
            } else if (randomValue < 0.9) {
                SdCommand.RemoveTerm(index)
            } else {
                SdCommand.NewSeed
            }
        }
        logger.info("Fetched idle command: $command")

        return command
    }

    companion object {
        private val logger = LoggerFactory.getLogger(IdleManager::class.java)
    }
}