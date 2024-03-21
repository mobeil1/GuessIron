package de.indie42.guessiron

import android.content.res.Configuration
import android.os.Build
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import java.text.DecimalFormatSymbols


enum class ScalaCalibrationMode(val value: Int) {
    UnDef(0), Ruler(1), Card(2), UserDef(3)
}

@Composable
fun ScalaCalibrationScreen(mode: Int = 0, viewModel: GuessIronViewModel, onBack: () -> Unit) {

    val caliMode = ScalaCalibrationMode.values().first { it.value == mode }

    val configuration = LocalConfiguration.current
    val isLandsacpe = Configuration.ORIENTATION_LANDSCAPE == configuration.orientation

    val winInsetsSystembars = dynamicSystemBar(isLandsacpe)

    var displayRotation = android.view.Surface.ROTATION_0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        displayRotation = LocalContext.current.display?.rotation ?: android.view.Surface.ROTATION_0
    }

    val guessIronUiState by viewModel.uiState.collectAsState()

    var calibratingDistance by rememberSaveable { mutableFloatStateOf(0F ) }
    var showHowToDialog by rememberSaveable { mutableStateOf(true) }
    var newScalaFactor by rememberSaveable { mutableFloatStateOf(guessIronUiState.scalaFactor) }

    val scope = rememberCoroutineScope()

    val minFactor = 0.5F
    val maxFactor = 3.0F

    Surface(
        modifier = Modifier.fillMaxSize()
     ) {
        Surface(
            modifier = Modifier
                .windowInsetsPadding(winInsetsSystembars)
                .fillMaxSize()
                .pointerInput(Unit) {
                    var switchTopForCenter = 0F
                    detectDragGestures(onDragStart = { offset ->
                        switchTopForCenter = if (isLandsacpe)
                            offset.x
                        else
                            offset.y
                    }) { _, dragAmount ->
                        val dragLength = if (isLandsacpe)
                            dragAmount.x
                        else
                            dragAmount.y

                        var scalaOrientation = guessIronUiState.scalaDirection
                        if (displayRotation == android.view.Surface.ROTATION_180 || displayRotation == android.view.Surface.ROTATION_270) {
                            scalaOrientation = when (scalaOrientation) {
                                ScalaDirection.Top -> ScalaDirection.Bottom
                                ScalaDirection.Bottom -> ScalaDirection.Top
                                else -> scalaOrientation
                            }
                        }

                        val y = when (scalaOrientation) {
                            ScalaDirection.Top -> dragLength
                            ScalaDirection.Bottom -> dragLength * -1
                            ScalaDirection.Center -> {
                                val height = if (isLandsacpe)
                                    size.width
                                else
                                    size.height
                                if (switchTopForCenter < height / 2)
                                    dragLength * -1
                                else
                                    dragLength
                            }
                        }
                        if (caliMode != ScalaCalibrationMode.UnDef) {
                            val scalaFactor = newScalaFactor + y / 2000
                            newScalaFactor = if (scalaFactor < minFactor)
                                minFactor
                            else if (scalaFactor > maxFactor)
                                maxFactor
                            else
                                scalaFactor
                        }

                    }
                },
            color = MaterialTheme.colorScheme.background
        ){
        ScalaBar(
            guessIronUiState.scalaDirection,
            ScalaPosition.Left,
            measuredDistance = calibratingDistance,
            scalaFactor = newScalaFactor,
            unitSystem = guessIronUiState.unitSystem
        )
        ScalaBar(
            guessIronUiState.scalaDirection,
            ScalaPosition.Right,
            measuredDistance = calibratingDistance,
            scalaFactor = newScalaFactor,
            unitSystem = guessIronUiState.unitSystem
        )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(text = AnnotatedString(text = stringResource(id = R.string.DragTheScala)))
            Text(
                text = stringResource(id = R.string.Factor) + ": " + String.format(
                    "%.2f",
                    newScalaFactor
                ),
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
            )

            if (isLandsacpe){
                Row (horizontalArrangement = Arrangement.Center) {
                    OutlinedButton( modifier = Modifier.padding(horizontal = 4.dp), onClick = onBack) {
                        Text(text = AnnotatedString(text = stringResource(id = R.string.Cancel)))
                    }
                    Button(modifier = Modifier.padding(horizontal = 4.dp), onClick = {
                        scope.launch {
                            viewModel.changeScalaFactor(newScalaFactor)
                        }
                        onBack() }) {
                        Text(text = AnnotatedString(text = stringResource(id = R.string.Ok)))
                    }
                }
            }
            else{
                CalibrationOkAndCancel(onOk = {
                    scope.launch {
                        viewModel.changeScalaFactor(newScalaFactor)
                    }
                    onBack() },
                    onBack = onBack)
            }

            if (showHowToDialog) {
                when (caliMode) {
                    ScalaCalibrationMode.Ruler -> {
                        CalibrationModeRuler(onOk = { showHowToDialog = false })
                    }
                    ScalaCalibrationMode.Card -> {
                        CalibrationModeCard(onOk = {
                            calibratingDistance = if (guessIronUiState.unitSystem.getUnitsystem() == UnitSystem.IMPERIAL) 53.98F / 2.54F else 53.98F
                            showHowToDialog = false })
                    }
                    ScalaCalibrationMode.UserDef -> {
                        CalibrationModeUserDef(onOk = {
                            calibratingDistance = it * guessIronUiState.unitSystem.getDecimal()
                            showHowToDialog = false
                        },
                            guessIronUiState.unitSystem.getUnit(), onBack)
                    }
                    else -> onBack()
                }
            }
        }
    }
}


@Composable
private fun CalibrationModeUserDef(
    onOk: (distance: Float) -> Unit,
    unit: String,
    onBack: () -> Unit
) {
    var showUserDefDialog by rememberSaveable { mutableStateOf(true) }
    var calibratingDistance by rememberSaveable { mutableFloatStateOf(0F ) }

    if (showUserDefDialog) {
        UserDefDialog(
            onDismissRequest = onBack,
            onConfirmation = {
                calibratingDistance = it
                showUserDefDialog = false
            },
            unit = unit
        )
    } else
        CalibrationInfoDialog(text = stringResource(id = R.string.InstructionsCalibrateWithDifferentItem),
            itemName = stringResource(id = R.string.Item),
            onDismissRequest = { onOk(calibratingDistance) })
}

@Composable
private fun CalibrationModeCard(onOk: () -> Unit) {
    CalibrationInfoDialog(
        text = stringResource(id = R.string.InstructionsCalibrateWithCreditcard),
        itemName = stringResource(id = R.string.Creditcard), onDismissRequest = onOk
    )
}

@Composable
private fun CalibrationModeRuler(
    onOk: () -> Unit
) {

    CalibrationInfoDialog(
        text = stringResource(id = R.string.InstructionsCalibrateWithRuler),
        measuredDistance = 0F,
        withCreditcard = false,
        onDismissRequest = onOk
    )
}

@Composable
private fun CalibrationOkAndCancel(
    onOk: () -> Unit,
    onBack: () -> Unit
) {

    Button(onClick = onOk) {
        Text(text = AnnotatedString(text = stringResource(id = R.string.Ok)))
    }

    OutlinedButton(onClick = onBack) {
        Text(text = AnnotatedString(text = stringResource(id = R.string.Cancel)))
    }
}

@Composable
fun UserDefDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (distance: Float) -> Unit,
    unit: String
) {
    var text by rememberSaveable { mutableStateOf("") }
    val focusInput = remember { FocusRequester() }

    val decimalFormatSymbols = DecimalFormatSymbols(LocalConfiguration.current.locales.get(0))

    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = stringResource(id = R.string.Ask_For_Distance),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp),
                )
                Row {
                    OutlinedTextField(
                        value = text,
                        modifier = Modifier
                            .padding(16.dp)
                            .focusRequester(focusInput),
                        onValueChange = {
                            text = it.replace("-", "")
                                      .replace(" ", "")
                                .replace(decimalFormatSymbols.groupingSeparator.toString(), "")


                        },
                        label = { Text(stringResource(id = R.string.Distance)) },
                        placeholder = { Text(unit) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        trailingIcon = {

                            Icon(imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(
                                    id = R.string.Delete
                                ),
                                modifier = Modifier.clickable {
                                    text = ""
                                    focusInput.requestFocus()
                                })

                        }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(stringResource(id = R.string.Cancel))
                    }
                    TextButton(
                        onClick = {
                            val distance = try { text.replace(decimalFormatSymbols.decimalSeparator.toString(), ".").toFloat()} catch (e: NumberFormatException) { null }
                            if (distance != null)
                                onConfirmation(distance)
                        },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(stringResource(id = R.string.Ok))
                    }
                }
            }
        }

        // Request focus as a SideEffect (after the composition)
        SideEffect {
            focusInput.requestFocus()
        }
    }


}

@Composable
fun CalibrationInfoDialog(
    text: String = "",
    itemName: String = "",
    measuredDistance: Float = 15F,
    withCreditcard: Boolean = true,
    onDismissRequest: () -> Unit,
) {

    val configuration = LocalConfiguration.current
    val isLandsacpe = Configuration.ORIENTATION_LANDSCAPE == configuration.orientation

    // Creates an [InfiniteTransition] instance for managing child animations.
    val infiniteTransition = rememberInfiniteTransition(label = "l1")

    // Creates a child animation of float type as a part of the [InfiniteTransition].
    val scale by infiniteTransition.animateFloat(
        initialValue = 50F,
        targetValue = 140f,
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

    Dialog(onDismissRequest = { onDismissRequest() }) {
        // Draw a rectangle shape with rounded corners inside the dialog
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(modifier = Modifier.height(200.dp)) {
                    Box(
                        modifier = Modifier
                    ) {
                        Surface(
                            modifier = Modifier
                                .padding( top =  if (isLandsacpe) 0.dp else 16.dp)
                                .height(200.dp),
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            ScalaBar(
                                scalaPosition = ScalaPosition.Left,
                                measuredDistance = measuredDistance,
                                scalaFactor = scale / 100
                            )
                        }
                        if (withCreditcard) {
                            Box(
                                modifier = Modifier.padding(start = if (isLandsacpe) 0.dp else 56.dp, top = if (isLandsacpe) 16.dp else 0.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .padding(if (isLandsacpe) 0.dp else 16.dp)
                                        .width( if (isLandsacpe ) 125.dp else 192.dp)
                                        .height( if (isLandsacpe ) 180.dp else 125.dp),
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                ) {
                                    Text(
                                        modifier = Modifier.padding(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        text = itemName
                                    )
                                }
                            }
                        }
                    }
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
                Button(
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth(),
                    onClick = { onDismissRequest() },
                ) {
                    Text(stringResource(id = R.string.Ok))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserDefDialogPreview() {
    //UserDefDialog(onDismissRequest = {}, onConfirmation = {})

    CalibrationInfoDialog(onDismissRequest = {})
}