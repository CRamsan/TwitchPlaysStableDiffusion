package ui

import Command
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import sd.Term

@Composable
fun CommandQueue(
    lastExecutedCommand: String,
    bufferCounter: Int,
    commandMap: Map<String, Command>,
    inIdleMode: Boolean,
    modifier: Modifier = Modifier,
) {
    Frame(
        modifier = modifier,
    ) {
        Column(
            modifier =  Modifier
        ) {
            if (inIdleMode) {
                Text(
                    "In idle mode. Random commands will be executed...",
                    modifier = Modifier.padding(bottom = 25.dp)
                )
            }
            Text("Last command was: $lastExecutedCommand")
            val message = if (bufferCounter < 0) {
                "Processing command..."
            } else {
                "Executing new command in: $bufferCounter"
            }

            Text(
                message,
                modifier = Modifier.padding(vertical = 25.dp)
            )
            commandMap.forEach { (username, command) ->
                Text("[$username] $command")
            }
        }
    }
}

@Composable
fun TextSection(
    text: String,
    modifier: Modifier = Modifier,
) {
    Frame(
        modifier = modifier,
    ) {
        Text(text,
            modifier = Modifier
                .wrapContentHeight(),
            style = MaterialTheme.typography.caption,
        )
    }
}

@Composable
fun ParametersSection(
    cfgScale: Double,
    width: Int,
    height: Int,
    samplingSteps: Int,
    modifier: Modifier = Modifier,
) {
    Frame (
        modifier = modifier,
    ) {
        Column {
            Text(
                "Parameters",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(vertical = Padding.medium)
            )
            Text("CFG Scale: $cfgScale")
            Text("Width: $width")
            Text("Height: $height")
            Text("Sampling Steps: $samplingSteps")
        }
    }
}

@Composable
fun PromptSection(
    label: String,
    promptList: List<Term>,
    modifier: Modifier = Modifier,
) {
    Frame(
        modifier = modifier.animateContentSize()
    ) {
        Column {
            Text(
                label,
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(vertical = Padding.medium)
            )
            promptList.forEachIndexed { index, term ->
                if (term.emphasis != 0) {
                    Text("[$index][Weight: ${term.emphasis}]: ${term.term}")
                } else {
                    Text("[$index]: ${term.term}")
                }
            }
        }
    }
}

@Composable
fun Background(
    imagePath: String?,
) {
    if (imagePath != null) {
        AsyncImage(
            path = imagePath,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(50.dp)
        )
    }
}

@Composable
fun BoxScope.MainImage(
    imagePath: String?,
    width: Int,
    height: Int,
    isLoading: Boolean,
) {
    if (imagePath != null) {
        val imageModifier = if (isLoading) {
            Modifier.blur(
                25.dp,
                BlurredEdgeTreatment(ROUNDED_CORNER_LARGE)
            )
        } else {
            Modifier
        }
            .padding(top = Padding.xxlarge)
            .size((width * 1.5).dp, (height * 1.5).dp)
            .clip(ROUNDED_CORNER_LARGE)
            .align(Alignment.TopCenter)

        AsyncImage(
            path = imagePath,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = imageModifier
        )
    }
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.Center)
    ) {
        Frame {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun Frame(
    modifier: Modifier = Modifier,
    shape: Shape = shapes.medium,
    border: BorderStroke = BorderStroke(Border.normal, MaterialTheme.colors.primary),
    marginPadding: Dp = Padding.large,
    alignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier
            .animateContentSize(),
        shape = shape,
        border = border,
        color = MaterialTheme.colors.primary.copy(alpha = OPACITY),
        contentColor = MaterialTheme.colors.onPrimary,
    ) {
        Box(
            modifier = Modifier.padding(marginPadding),
            contentAlignment = alignment,
        ) {
            content()
        }
    }
}

@Composable
fun Instructions(
    modifier: Modifier = Modifier,
) {
    Frame(modifier) {
        Column {
            Text(
                "Instructions",
                style = MaterialTheme.typography.h5,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Row {
                Text(
                    INSTRUCTIONS_PT1,
                    style = MaterialTheme.typography.body2,
                )
                Divider(Modifier.width(Padding.large))
                Text(
                    INSTRUCTIONS_PT2,
                    style = MaterialTheme.typography.body2,
                )
            }
        }
    }
}

private const val INSTRUCTIONS_PT1 = """
// To update the positive prompt use `prompt`
// and for the negative prompt use `nprompt`.   
sd prompt add 0 cat looking up
sd prompt add 2 happy
sd prompt update 2 very happy
sd prompt remove 5
sd prompt increment 8
sd prompt decrement 2

// Change the CFG Index
sd cfg increment
sd cfg decrement
"""

private const val INSTRUCTIONS_PT2 = """
// Change the output height
sd height increment
sd height decrement

// Change the output width
sd width increment
sd width decrement

// Change the number of sampling steps
sd steps increment
sd steps decrement
"""