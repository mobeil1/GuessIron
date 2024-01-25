package de.indie42.guessiron

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun MeasureToEdgeCalibrationScreen(
    offsetScalaDirection: Int = 0,
    startOffset: Int = 0,
    viewModel: GuessIronViewModel,
    onBack: () -> Unit
) {

    val scalaDirection = ScalaDirection.values().first { it.value == offsetScalaDirection }

    val guessIronUiState by viewModel.uiState.collectAsState()
    val guessIronDataState by viewModel.dataState.collectAsState()

    var edgePixelOffset by rememberSaveable {
        mutableFloatStateOf(
            viewModel.calculatePixelFromMM(
                startOffset
            ) * -1
        )
    }

    var edgeMMOffset by remember {
        mutableIntStateOf(startOffset)
    }


    val configuration = LocalConfiguration.current
    val isLandsacpe = Configuration.ORIENTATION_LANDSCAPE == configuration.orientation

    var displayRotation by remember { mutableIntStateOf(android.view.Surface.ROTATION_0)}

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        displayRotation = LocalContext.current.display?.rotation ?: android.view.Surface.ROTATION_0
    }

    DynamicSystemBar(true)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(onDragStart = { _ ->
                }) { _, dragAmount ->
                    var dragOffset = if (isLandsacpe)
                        dragAmount.x
                    else
                        dragAmount.y

                    if (displayRotation == android.view.Surface.ROTATION_180 || displayRotation == android.view.Surface.ROTATION_270) {
                        dragOffset *= -1
                    }

                    if (scalaDirection == ScalaDirection.Bottom)
                        dragOffset *= -1

                    edgePixelOffset += dragOffset
                    if (edgePixelOffset > 0)
                        edgePixelOffset = 0F

                    edgeMMOffset = viewModel.calculateMMFromPixel(edgePixelOffset, 0)


                }
            },
        color = MaterialTheme.colorScheme.background
    ) {
        ScalaBar(
            scalaDirection,
            ScalaPosition.Left,
            scalaFactor = guessIronUiState.scalaFactor,
            scalaStartMM = edgeMMOffset
        )
        ScalaBar(
            scalaDirection,
            ScalaPosition.Right,
            scalaFactor = guessIronUiState.scalaFactor,
            scalaStartMM = edgeMMOffset
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {


            if (isLandsacpe) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DragTheScaleHowToImage(scalaDirection)
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        ConfigOffset(
                            edgeMMOffset,
                            onBack,
                            scalaDirection,
                            viewModel,
                            guessIronDataState
                        )
                    }
                }
            } else {
                DragTheScaleHowToImage(scalaDirection)

                ConfigOffset(
                    edgeMMOffset,
                    onBack,
                    scalaDirection,
                    viewModel,
                    guessIronDataState
                )
            }


        }
    }
}

@Composable
private fun ConfigOffset(
    edgeMMOffset: Int,
    onBack: () -> Unit,
    scalaDirection: ScalaDirection,
    viewModel: GuessIronViewModel,
    guessIronDataState: GuessIronDataState
) {
    val scope = rememberCoroutineScope()

    Text(
        modifier = Modifier.padding(8.dp),
        text = AnnotatedString(text = stringResource(id = R.string.Margin) + ": $edgeMMOffset mm"),
        style = MaterialTheme.typography.bodyLarge
    )

    Button(modifier = Modifier.padding(6.dp), onClick = {
        scope.launch {
            if (scalaDirection == ScalaDirection.Top)
                viewModel.changeDisplayBorder(
                    edgeMMOffset,
                    guessIronDataState.displayBorder.bottom
                )
            else if (scalaDirection == ScalaDirection.Bottom)
                viewModel.changeDisplayBorder(
                    guessIronDataState.displayBorder.top,
                    edgeMMOffset
                )
        }
        onBack()
    }
    ) {
        Text(text = AnnotatedString(text = stringResource(id = R.string.Ok)))
    }
    OutlinedButton(
        modifier = Modifier.padding(6.dp),
        onClick = onBack
    ) {
        Text(text = AnnotatedString(text = stringResource(id = R.string.Cancel)))
    }
}

@Composable
fun MeasureToEdgeCalibrationHelp(scalaDirection: ScalaDirection = ScalaDirection.Top) {

    val infiniteTransition = rememberInfiniteTransition(label = "l1")

    // Creates a child animation of float type as a part of the [InfiniteTransition].
    val scale by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 10F,
        animationSpec = infiniteRepeatable(
            // Infinitely repeating a 1000ms tween animation using default easing curve.
            animation = tween(1500),
            // After each iteration of the animation (i.e. every 1000ms), the animation will
            // start again from the [initialValue] defined above.
            // This is the default [RepeatMode]. See [RepeatMode.Reverse] below for an
            // alternative.
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    val primaryContainer = MaterialTheme.colorScheme.primaryContainer

    Surface(
        modifier = Modifier
            .size(200.dp, 175.dp)
            .scale(0.75F),
        color = MaterialTheme.colorScheme.background
    ) {
        Box( modifier = Modifier
            .size(200.dp, 175.dp)
            .rotate(if (scalaDirection == ScalaDirection.Bottom) 180F else 0F)
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(color = Color.DarkGray, size = Size(200.dp.toPx(), 9.dp.toPx()))
                    val myPath = Path().apply {
                        addRoundRect(
                            RoundRect(
                                rect = Rect(
                                    offset = Offset(90.dp.toPx(), 10.dp.toPx()),
                                    size = Size(125.dp.toPx(), size.height)
                                ),
                                topLeft = CornerRadius(100F, 100F),
                                topRight = CornerRadius(10F, 10F),
                            )
                        )
                    }
                    drawPath(myPath, color = primaryContainer)
                    if (scalaDirection == ScalaDirection.Top) {
                        drawCircle(
                            color = Color.Black,
                            radius = 10.dp.toPx(),
                            center = Offset(x = 185.dp.toPx(), y = 30.dp.toPx())
                        )
                    }
                }
                .padding(top = 10.dp, end = 125.dp)) {
            }
        }
        Box(
            modifier = Modifier
                .size(200.dp, 175.dp)
                .padding(
                    start = if (scalaDirection == ScalaDirection.Bottom) 0.dp else 90.dp,
                    top = if (scalaDirection == ScalaDirection.Bottom) 0.dp else 66.dp,
                    end = if (scalaDirection == ScalaDirection.Bottom) 90.dp else 0.dp,
                    bottom = if (scalaDirection == ScalaDirection.Bottom) 60.dp else 0.dp)
        ) {
            ScalaBar(
                direction = scalaDirection,
                scalaPosition = if (scalaDirection == ScalaDirection.Bottom) ScalaPosition.Right else ScalaPosition.Left,
                scalaFactor = 1F,
                scalaStartMM = scale.toInt(),
                supportLandscapeMode = false
            )
        }
        Box(modifier = Modifier
            .size(200.dp, 175.dp)
            .padding(
                start = if (scalaDirection == ScalaDirection.Bottom) 125.dp else 0.dp,
                top = if (scalaDirection == ScalaDirection.Bottom) 0.dp else 16.dp,
                end = if (scalaDirection == ScalaDirection.Bottom) 0.dp else 125.dp,
                bottom = if (scalaDirection == ScalaDirection.Bottom) 8.dp else 0.dp
            ) ){
            ScalaBar(
                direction = scalaDirection,
                scalaPosition = if (scalaDirection == ScalaDirection.Bottom) ScalaPosition.Left else ScalaPosition.Right,
                scalaFactor = 1F,
                supportLandscapeMode = false,
            )
        }
    }
}


@Composable
private fun DragTheScaleHowToImage(scalaDirection: ScalaDirection = ScalaDirection.Top) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = AnnotatedString(text = stringResource(id = R.string.DragTheScala)))
            MeasureToEdgeCalibrationHelp(scalaDirection)
        }
    }
}
