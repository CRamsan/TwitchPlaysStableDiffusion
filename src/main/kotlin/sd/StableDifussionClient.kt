package sd

import SdCommand
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.util.Base64
import kotlin.random.Random

/**
 * Client to interface with SD. It holds the state of the context currently being used
 * as arguments for SD.
 */
class StableDiffusionClient(
    private val client: HttpClient,
    private val json: Json,
) {

    private var _uiState = MutableStateFlow(UIState(
        cfgScale = 7.5,
        width = 512,
        height = 512,
        samplingSteps = 24,
        lastExecutedCommand = "None",
        imagePath = null,
        isLoading = false,
        positivePrompt = INITIAL_POSITIVE_PROMPT,
        negativePrompt = INITIAL_NEGATIVE_PROMPT,
        positivePromptText = "...",
        negativePromptText = "...",
    ))

    // Observable the contains the latest parameters and output
    val uiState = _uiState.asStateFlow()

    private var seed = Random.nextLong()

    /**
     * Send a [command] to be executed. This function is safe to be called from any thread as it will
     * ensure so dispatch to a background thread. The operation is run synchronously but the result
     * should be observed through [uiState].
     */
    suspend fun handleCommands(command: SdCommand?) = withContext(Dispatchers.IO) {
        if (command == null) {
            logger.debug("Null command")
            return@withContext
        }

        logger.info("Handling $command")
        val executedCommand: String = when (command) {
            SdCommand.DecrementCFGScale -> { changeCfgScale(-CFG_CHANGE) }
            SdCommand.IncrementCFGScale -> { changeCfgScale(CFG_CHANGE) }
            is SdCommand.IncrementHeight -> { changeHeight(SIZE_CHANGE) }
            is SdCommand.IncrementWidth -> { changeWidth(SIZE_CHANGE) }
            is SdCommand.DecrementHeight -> { changeHeight(-SIZE_CHANGE) }
            is SdCommand.DecrementWidth -> { changeWidth(SIZE_CHANGE) }
            SdCommand.NewSeed -> { changeSeed() }
            SdCommand.DecrementSamplingSteps -> { changeStepCount(-1) }
            SdCommand.IncrementSamplingSteps -> { changeStepCount(1) }
            is SdCommand.AddTerm -> { addPositiveTerm(command.position, command.term) }
            is SdCommand.UpdateTerm -> { updatePositiveTerm(command.position, command.term) }
            is SdCommand.RemoveTerm -> { removePositiveTerm(command.position) }
            is SdCommand.AddNegativeTerm -> { addNegativeTerm(command.position, command.term) }
            is SdCommand.UpdateNegativeTerm -> { updateNegativeTerm(command.position, command.term) }
            is SdCommand.RemoveNegativeTerm -> { removeNegativeTerm(command.position) }
            is SdCommand.EmphasizeTerm -> { emphasizeTerm(command.position) }
            is SdCommand.EmphasizeNegativeTerm -> { emphasizeNegativeTerm(command.position) }
            is SdCommand.DeemphasizeTerm -> { deemphasizePositivePrompt(command.position) }
            is SdCommand.DeemphasizeNegativeTerm -> { deemphasizeNegativePrompt(command.position) }
            SdCommand.Noop -> "Noop"
        }

        _uiState.mutate {
            copy(
                lastExecutedCommand = executedCommand,
                positivePrompt = positivePrompt.toList(),
                negativePrompt = negativePrompt.toList(),
                positivePromptText = positivePrompt.toPrompt(),
                negativePromptText = negativePrompt.toPrompt(),
            )
        }

        val body = Txt2ImgRequest(
            prompt = uiState.value.positivePromptText,
            negativePrompt = uiState.value.negativePromptText,
            sendImages = true,
            saveImages = false,
            seed = seed,
            steps = uiState.value.samplingSteps,
            cfgScale = uiState.value.cfgScale,
            width = uiState.value.width,
            height = uiState.value.height,
            samplerIndex = "DPM++ 2S a Karras",
        )

        runCommand(body)
    }

    private suspend fun runCommand(request: Txt2ImgRequest) {
        logger.info("Sending request: ${json.encodeToString(request)}")

        _uiState.mutate {
            copy(
                isLoading = true,
            )
        }
        val response: Txt2ImgResponse = client.post("http://127.0.0.1:7860/sdapi/v1/txt2img") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        val resultImageEncoded = response.images.firstOrNull()
        val imageBytes = Base64.getDecoder().decode(resultImageEncoded)

        val file = kotlin.io.path.createTempFile(
            suffix = ".jpg"
        ).toFile()
        file.writeBytes(imageBytes)

        logger.info("Saving image: ${file.absoluteFile}")

        _uiState.mutate {
            copy(
                imagePath = file.absolutePath,
                isLoading = false,
            )
        }
    }

    private fun changeCfgScale(change: Float): String {
        val cfg = uiState.value.cfgScale
        if ((cfg - change) > 0 || (cfg + change) < 20) {
            _uiState.value = uiState.value.copy(
                cfgScale = cfg + change,
            )
        }
        return "Changed CFG by $change"
    }

    private fun changeHeight(change: Int): String {
        val newHeight = _uiState.value.height + change
        if (newHeight in 201..799) {
            _uiState.value = uiState.value.copy(
                height = newHeight,
            )
        }
        return "Changed height by $change"
    }

    private fun changeWidth(change: Int): String {
        val newWidth = _uiState.value.width + change
        if (newWidth in 201..799) {
            _uiState.value = uiState.value.copy(
                width = newWidth,
            )
        }
        return "Changed width by $change"
    }

    private fun changeStepCount(change: Int): String {
        val newSamplingSteps = uiState.value.samplingSteps + change
        if (newSamplingSteps in 11..39) {
            _uiState.value = _uiState.value.copy(
                samplingSteps = newSamplingSteps,
            )
        }
        return "Changed sampling-steps by $change"
    }

    private fun emphasizeTerm(position: Int): String {
        val positivePrompt = _uiState.value.positivePrompt.toMutableList()

        if (positivePrompt.isEmpty() || position !in 0..positivePrompt.lastIndex) {
            return ""
        }

        positivePrompt[position] = positivePrompt[position].emphasize()

        _uiState.value = _uiState.value.copy(
            positivePrompt = positivePrompt
        )
        return "Emphasized term ${uiState.value.positivePrompt[position].term}"
    }

    private fun emphasizeNegativeTerm(position: Int): String {
        val negativePrompt = _uiState.value.negativePrompt.toMutableList()

        if (negativePrompt.isEmpty() || position !in 0..negativePrompt.lastIndex) {
            return ""
        }

        negativePrompt[position] = negativePrompt[position].emphasize()

        _uiState.value = _uiState.value.copy(
            negativePrompt = negativePrompt
        )
        return "Emphasized negative term ${uiState.value.negativePrompt[position].term}"
    }

    private fun deemphasizePositivePrompt(position: Int): String {
        val positivePrompt = _uiState.value.positivePrompt.toMutableList()

        if (positivePrompt.isEmpty() || position !in 0..positivePrompt.lastIndex) {
            return ""
        }

        positivePrompt[position] = positivePrompt[position].deemphasize()

        _uiState.value = _uiState.value.copy(
            positivePrompt = positivePrompt
        )
        return "Deemphasized term ${uiState.value.positivePrompt[position].term}"
    }

    private fun deemphasizeNegativePrompt(position: Int): String {
        val negativePrompt = _uiState.value.negativePrompt.toMutableList()

        if (negativePrompt.isEmpty() || position !in 0..negativePrompt.lastIndex) {
            return ""
        }

        negativePrompt[position] = negativePrompt[position].deemphasize()

        _uiState.value = _uiState.value.copy(
            negativePrompt = negativePrompt
        )
        return "Deemphasized negative term ${uiState.value.negativePrompt[position].term}"
    }

    private fun removePositiveTerm(position: Int): String {
        val positivePrompt = _uiState.value.positivePrompt.toMutableList()

        if (positivePrompt.isEmpty() || position !in 0..positivePrompt.lastIndex) {
            return ""
        }

        positivePrompt.removeAt(position)

        _uiState.value = _uiState.value.copy(
            positivePrompt = positivePrompt
        )
        return "Removed term ${uiState.value.positivePrompt[position].term}"
    }

    private fun updatePositiveTerm(position: Int, term: String): String {
        val positivePrompt = _uiState.value.positivePrompt.toMutableList()

        if (positivePrompt.isEmpty() || position !in 0..positivePrompt.lastIndex) {
            return ""
        }

        val oldTerm = positivePrompt[position].term
        positivePrompt[position] = Term(term)

        _uiState.value = _uiState.value.copy(
            positivePrompt = positivePrompt
        )
        return "Updated term from $oldTerm to ${uiState.value.positivePrompt[position].term}"
    }

    private fun addPositiveTerm(position: Int, term: String): String {
        val positivePrompt = _uiState.value.positivePrompt.toMutableList()
        if (positivePrompt.size >= 50){
            return ""
        }
        if (position >= 0)
            positivePrompt.add(position, Term(term))
        else
            positivePrompt.add(positivePrompt.size + position + 1, Term(term))
        _uiState.value = _uiState.value.copy(
            positivePrompt = positivePrompt
        )

        return "Added term $term in position $position"
    }


    private fun removeNegativeTerm(position: Int): String {
        val negativePrompt = _uiState.value.negativePrompt.toMutableList()

        if (negativePrompt.isEmpty() || position !in 0..negativePrompt.lastIndex) {
            return ""
        }

        negativePrompt.removeAt(position)

        _uiState.value = _uiState.value.copy(
            negativePrompt = negativePrompt
        )
        return "Removed negative term ${uiState.value.positivePrompt[position].term}"
    }

    private fun updateNegativeTerm(position: Int, term: String): String {
        val negativePrompt = _uiState.value.negativePrompt.toMutableList()

        if (negativePrompt.isEmpty() || position !in 0..negativePrompt.lastIndex) {
            return ""
        }

        val oldTerm = negativePrompt[position]
        negativePrompt[position] = Term(term)

        _uiState.value = _uiState.value.copy(
            negativePrompt = negativePrompt
        )
        return "Updated negative term from $oldTerm to ${uiState.value.negativePrompt[position].term}"
    }

    private fun addNegativeTerm(position: Int, term: String): String {
        val negativePrompt = _uiState.value.negativePrompt.toMutableList()
        if (negativePrompt.size >= 50){
            return ""
        }
        if (position >= 0)
            negativePrompt.add(position, Term(term))
        else
            negativePrompt.add(negativePrompt.size + position + 1, Term(term))
        _uiState.value = _uiState.value.copy(
            negativePrompt = negativePrompt
        )
        return "Added negative term $term in position $position"
    }

    private fun changeSeed(): String {
        seed = Random.nextLong()
        return "Changed seed"
    }

    companion object {
        private val logger = LoggerFactory.getLogger(StableDiffusionClient::class.java)
    }
}

private const val CFG_CHANGE = 0.1f
private const val SIZE_CHANGE = 8
