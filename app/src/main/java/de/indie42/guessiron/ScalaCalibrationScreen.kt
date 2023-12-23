package de.indie42.guessiron

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch


enum class ScalaCalibrationMode {
    UnDef, Ruler, Card, UserDef
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScalaCalibrationScreen(viewModel: GuessIronViewModel, onBack: () -> Unit) {

    val guessIronDataState by viewModel.dataState.collectAsState()
    val guessIronUiState by viewModel.uiState.collectAsState()

    var calibratingDistance by remember { mutableIntStateOf(0) }
    var caliMode by remember { mutableStateOf(ScalaCalibrationMode.UnDef) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showUserDefDialog by remember { mutableStateOf(false) }
    var newScalaFactor by remember { mutableFloatStateOf(guessIronDataState.scalaFactor) }

    val minFactor = 0.5F
    val maxFactor = 3.0F

    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                title = {
                    Text(stringResource(id = R.string.calibration))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.Back)
                        )
                    }
                },

                )
        },

        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .pointerInput(Unit) {
                    var switchTopForCenter = 0F
                    detectDragGestures(onDragStart = { offset ->
                        switchTopForCenter = offset.y
                    }) { _, dragAmount ->
                        var y = when(guessIronUiState.scalaDirection){
                            ScalaDirection.Top -> dragAmount.y
                            ScalaDirection.Bottom -> dragAmount.y * -1
                            ScalaDirection.Center -> if ( switchTopForCenter < size.height/2)
                                dragAmount.y * -1
                            else
                                dragAmount.y
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
        ) {
            if (caliMode != ScalaCalibrationMode.UnDef) {
                ScalaBar(
                    guessIronUiState.scalaDirection,
                    ScalaPosition.Left,
                    measuredMM = calibratingDistance,
                    scalaFactor = newScalaFactor
                )
                ScalaBar(
                    guessIronUiState.scalaDirection,
                    ScalaPosition.Right,
                    measuredMM = calibratingDistance,
                    scalaFactor = newScalaFactor
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                if (caliMode == ScalaCalibrationMode.UnDef) {

                    Text(text = AnnotatedString(text = stringResource(id = R.string.AskHowToCalibrate)))

                    OutlinedButton(onClick = {
                        caliMode = ScalaCalibrationMode.Ruler
                        showInfoDialog = true
                    }) {
                        Text(text = AnnotatedString(text = stringResource(id = R.string.WithARuler)))
                    }
                    OutlinedButton(onClick = {
                        caliMode = ScalaCalibrationMode.Card
                        showInfoDialog = true
                        calibratingDistance = 54
                    }) {
                        Text(text = AnnotatedString(text = stringResource(id = R.string.WithACreditcard)))
                    }
                    OutlinedButton(onClick = {
                        showUserDefDialog = true
                    }) {
                        Text(text = AnnotatedString(text = stringResource(id = R.string.WithAnotherItem)))
                    }
                    FilledTonalButton(onClick = {
                        scope.launch {
                            newScalaFactor = 1.0F
                            viewModel.changeScalaFactor(newScalaFactor )
                        }
                        onBack() }) {
                        Text(text = AnnotatedString(text = stringResource(id = R.string.ResetCalibration)))
                    }

                } else if (caliMode == ScalaCalibrationMode.Ruler) {
                    if (showInfoDialog)
                        CalibrationInfoDialog(text = stringResource(id = R.string.InstructionsCalibrateWithRuler), measuredMM = 0, withCreditcard = false, onDismissRequest = {
                            showInfoDialog = false
                        })
                    else {
                        Text(text = AnnotatedString(text = stringResource(id = R.string.DragTheScala)))
                        Text(
                            text = stringResource(id = R.string.Factor ) + ": " + String.format("%.2f", newScalaFactor),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Button(onClick = {
                            scope.launch {
                                viewModel.changeScalaFactor(newScalaFactor)
                            }
                            onBack()
                        }) {
                            Text(text = AnnotatedString(text = stringResource(id = R.string.Ok)))
                        }

                        OutlinedButton(onClick = onBack) {
                            Text(text = AnnotatedString(text = stringResource(id = R.string.Cancel)))
                        }
                    }
                } else if (caliMode == ScalaCalibrationMode.Card) {
                    if (showInfoDialog)
                        CalibrationInfoDialog(text = stringResource(id = R.string.InstructionsCalibrateWithCreditcard),
                            itemName = stringResource(id = R.string.Creditcard), onDismissRequest = {
                            showInfoDialog = false
                        })
                    else {
                        Text(text = AnnotatedString(text = stringResource(id = R.string.DragTheScala)))
                        Text(
                            text = stringResource(id = R.string.Factor) + ": " + String.format("%.2f", newScalaFactor),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Button(onClick = {
                            scope.launch {
                                viewModel.changeScalaFactor(newScalaFactor)
                            }
                            onBack()
                        }) {
                            Text(text = AnnotatedString(text = stringResource(id = R.string.Ok)))
                        }
                        OutlinedButton(onClick = onBack) {
                            Text(text = AnnotatedString(text = stringResource(id = R.string.Cancel)))
                        }
                    }
                } else if (caliMode == ScalaCalibrationMode.UserDef) {
                    if (showInfoDialog)
                        CalibrationInfoDialog(text = stringResource(id = R.string.InstructionsCalibrateWithDifferentItem),
                            itemName = stringResource(id = R.string.Item), onDismissRequest = {
                            showInfoDialog = false
                        })
                    else {
                        Text(text = AnnotatedString(text = stringResource(id = R.string.DragTheScala)))
                        Text(
                            text = stringResource(id = R.string.Factor) + ": " + String.format("%.2f", newScalaFactor),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                        )

                        Button(onClick = {
                            scope.launch {
                                viewModel.changeScalaFactor(newScalaFactor)
                            }
                            onBack()
                        }) {
                            Text(text = AnnotatedString(text = stringResource(id = R.string.Ok)))
                        }
                        OutlinedButton(onClick = onBack) {
                            Text(text = AnnotatedString(text = stringResource(id = R.string.Cancel)))
                        }
                    }
                }

            }
        }
    }

    if (showUserDefDialog){
        UserDefDialog(
            onDismissRequest = {
                caliMode = ScalaCalibrationMode.UnDef
                showUserDefDialog = false
                               },
            onConfirmation = {
                calibratingDistance = it
                showInfoDialog = true
                caliMode = ScalaCalibrationMode.UserDef
                showUserDefDialog = false
            }
        )
    }
}

@Composable
fun UserDefDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: (distance: Int) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val focusInput = remember { FocusRequester() }

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

                Text(
                    text = stringResource(id = R.string.Ask_For_Distance),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
                Row {
                    OutlinedTextField(
                        value = text,
                        modifier = Modifier
                            .padding(16.dp)
                            .focusRequester(focusInput),
                        onValueChange = {
                            text = it
                                .replace(".", "")
                                .replace(",", "")
                                .replace("-", "")
                                .replace(" ", "")},
                        label = { Text(stringResource(id = R.string.Distance)) },
                        placeholder = { Text( "mm" ) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        trailingIcon = {

                            Icon(imageVector = Icons.Filled.Clear, contentDescription = stringResource(
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
                            if (text != "")
                                onConfirmation(text.toInt())
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
    measuredMM: Int = 15,
    withCreditcard: Boolean = true,
    onDismissRequest: () -> Unit,
) {

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
                Row (  modifier = Modifier.height(200.dp) ) {
                    Box(
                        modifier = Modifier
                    ) {
                        Surface(
                            modifier = Modifier
                                .padding(16.dp)
                                .height(200.dp)
                            ,
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            ScalaBar(scalaPosition = ScalaPosition.Left, measuredMM = measuredMM, scalaFactor = scale/100)
                        }
                        if (withCreditcard){
                            Box(
                                modifier = Modifier.padding(start = 56.dp)
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .width(192.dp)
                                        .height(125.dp)
                                    ,
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                ) {
                                    Text(modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onPrimaryContainer, text = itemName)
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
                        .padding(16.dp)
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
    UserDefDialog(onDismissRequest = {}, onConfirmation = {})
}