package sd

import kotlinx.coroutines.flow.MutableStateFlow

data class UIState(
    val cfgScale: Double,
    val width: Int,
    val height: Int,
    val samplingSteps: Int,
    val lastExecutedCommand: String,
    val imagePath: String?,
    val isLoading: Boolean,
    val positivePrompt: List<Term>,
    val negativePrompt: List<Term>,
    val positivePromptText: String,
    val negativePromptText: String,
)

fun <T> MutableStateFlow<T>.mutate(block: T.() -> T) {
    value = value.block()
}