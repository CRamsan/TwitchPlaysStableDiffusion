package ui

import Command
import SdCommand
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import kotlinx.coroutines.flow.StateFlow
import sd.StableDiffusionClient
import sd.Term

fun pageGUI(
    sdClient: StableDiffusionClient,
    bufferCounterState: StateFlow<Int>,
    commandMapState: StateFlow<Map<String, Command>>,
    inIdleModeState: StateFlow<Boolean>,
) {
    singleWindowApplication(
        undecorated = true,
        state = WindowState(
            size = DpSize(1920.dp, 1080.dp),
            position = WindowPosition.Absolute(0.dp, 0.dp)
        ),
        transparent = true,
    ) {
        val uiState by sdClient.uiState.collectAsState()

        val bufferCounter = bufferCounterState.collectAsState()
        val commandMap = commandMapState.collectAsState()
        val inIdleMode = inIdleModeState.collectAsState()

        Page(
            cfgScale = uiState.cfgScale,
            width = uiState.width,
            height = uiState.height,
            samplingSteps = uiState.samplingSteps,
            lastExecutedCommand = uiState.lastExecutedCommand,
            positivePrompt = uiState.positivePrompt,
            negativePrompt = uiState.negativePrompt,
            isLoading = uiState.isLoading,
            imagePath = uiState.imagePath,
            positivePromptText = uiState.positivePromptText,
            negativePromptText = uiState.negativePromptText,
            bufferCounter = bufferCounter.value,
            commandMap = commandMap.value,
            inIdleMode = inIdleMode.value,
        )
    }
}

@Composable
fun Page(
    cfgScale: Double,
    width: Int,
    height: Int,
    samplingSteps: Int,
    lastExecutedCommand: String,
    positivePrompt: List<Term>,
    negativePrompt: List<Term>,
    isLoading: Boolean,
    imagePath: String?,
    positivePromptText: String,
    negativePromptText: String,
    bufferCounter: Int,
    commandMap: Map<String, Command>,
    inIdleMode: Boolean,
) {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            Background(imagePath)

            MainImage(
                imagePath,
                width,
                height,
                isLoading,
            )

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(COLUMN_WIDTH)
            ) {
                PromptSection(
                    "Positive Prompt",
                    positivePrompt,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(5f)
                        .padding(Padding.large),
                )

                PromptSection(
                    "Negative Prompt",
                    negativePrompt,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(5f)
                        .padding(Padding.large),
                )
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(MAX_COLUMN_WIDTH)
                    .fillMaxHeight()
            ) {
                ParametersSection(
                    cfgScale,
                    width,
                    height,
                    samplingSteps,
                    modifier = Modifier
                        .width(COLUMN_WIDTH)
                        .padding(Padding.large),
                )
                CommandQueue(
                    lastExecutedCommand,
                    bufferCounter,
                    commandMap,
                    inIdleMode,
                    modifier =  Modifier
                        .weight(1f)
                        .padding(Padding.large),
                )
                TextSection(
                "Positive Prompt: $positivePromptText",
                    modifier = Modifier
                        .width(COLUMN_WIDTH)
                        .padding(Padding.large),
                )
                TextSection(
                    "Negative Prompt: $negativePromptText",
                    modifier = Modifier
                        .width(COLUMN_WIDTH)
                        .padding(Padding.large),
                )
            }
            Instructions(
                modifier = Modifier
                    .padding(Padding.large)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

val COLUMN_WIDTH = 350.dp
val MAX_COLUMN_WIDTH = 500.dp

const val OPACITY = 0.7f

@Preview
@Composable
internal fun PreviewScreen() {
    Page(
        cfgScale = 7.5,
        width = 512,
        height = 704,
        samplingSteps = 40,
        lastExecutedCommand = "Sampling Steps + 1",
        positivePrompt = listOf(
            Term("this is something", 1),
            Term("cute", 0),
            Term("color red", -1),
            Term("cat", +4),
            Term("dog", 1),
        ),
        negativePrompt = listOf(
            Term("this is another term", 0),
            Term("man", -3),
            Term("flower", +3),
            Term("example", -1),
            Term("ai", 0),
            Term("realistic", 0),
        ),
        isLoading = true,
        imagePath = "",
        positivePromptText = "a closeup portrait of a playful maid, undercut hair, apron, amazing body, pronounced feminine feature, busty, kitchen, [ash blonde | ginger | pink hair], freckles, flirting with cameraa closeup portrait of a playful maid, undercut hair, apron, amazing body, pronounced feminine feature, busty, kitchen, [ash blonde | ginger | pink hair], freckles, flirting with camera",
        negativePromptText = "a closeup portrait of a playful maid, undercut hair, apron, amazing body, pronounced feminine feature, busty, kitchen, [ash blonde | ginger | pink hair], freckles, flirting with cameraa closeup portrait of a playful maid, undercut hair, apron, amazing body, pronounced feminine feature, busty, kitchen, [ash blonde | ginger | pink hair], freckles, flirting with cameraa closeup portrait of a playful maid, undercut hair, apron, amazing body, pronounced feminine feature, busty, kitchen, [ash blonde | ginger | pink hair], freckles, flirting with cameraa closeup portrait of a playful maid, undercut hair, apron, amazing body, pronounced feminine feature, busty, kitchen, [ash blonde | ginger | pink hair], freckles, flirting with camera",
        bufferCounter = 5,
        commandMap = mapOf(
            "cramsan" to SdCommand.EmphasizeTerm(2),
            "empathy" to SdCommand.RemoveTerm(2),
        ),
        inIdleMode = true,
    )
}