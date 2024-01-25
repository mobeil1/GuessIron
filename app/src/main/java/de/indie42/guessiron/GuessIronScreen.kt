
package de.indie42.guessiron

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GuessIronScreen(
    onClickShowMeasured: () -> Unit,
    onShowDisclaimer: () -> Unit,
    onShowSetting: () -> Unit,
    onConfigDisplayBorder: () -> Unit,
    /*onClickCameraMeasure: () -> Unit,*/
    viewModel: GuessIronViewModel = viewModel(),
) {

    val guessIronState by viewModel.uiState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var showConfigureDisplayBorder by rememberSaveable { mutableStateOf(false) }
    var showMeasureToEdgeNotSupported by rememberSaveable { mutableStateOf(false) }
    var showMeasuredValueToBig by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    if (!guessIronState.disclaimerDisabled){
        onShowDisclaimer()
    }

    val configuration = LocalConfiguration.current
    val isLandsacpe = Configuration.ORIENTATION_LANDSCAPE == configuration.orientation


    var displayRotation by remember {
        mutableIntStateOf(android.view.Surface.ROTATION_0)
    }


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        displayRotation = LocalContext.current.display?.rotation ?: android.view.Surface.ROTATION_0
    }
    else{
        if (isLandsacpe && guessIronState.scalaOffsetActive)
            showMeasureToEdgeNotSupported = true
    }

    if (/*Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&*/ guessIronState.scalaOffsetActive) {
        /*val displayRotation = LocalContext.current.display?.rotation ?: 0
        if (displayRotation == android.view.Surface.ROTATION_270)*/
        DynamicSystemBar(true)
    }
    else
        DynamicSystemBar(isLandsacpe)

    var sizeInDp by remember { mutableStateOf(IntSize.Zero) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { }
    ) {
        // A surface container using the 'background' color from the theme
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { sizeInDp = it }
                .pointerInput(Unit) {
                    var switchTopForCenter = 0F
                    detectDragGestures(onDragStart = { offset ->
                        switchTopForCenter = if (isLandsacpe)
                            offset.x
                        else
                            offset.y
                    }) { _, dragAmount ->
                        if (isLandsacpe)
                            viewModel.increaseMeasuredPixel(
                                dragAmount.x,
                                switchTopForCenter,
                                sizeInDp,
                                displayRotation
                            )
                        else
                            viewModel.increaseMeasuredPixel(
                                dragAmount.y,
                                switchTopForCenter,
                                sizeInDp,
                                displayRotation
                            )
                    }
                }
                .pointerInput(Unit) {
                    if (isLandsacpe)
                        detectTapGestures { offset ->
                            viewModel.updateMeasuredPixel(
                                offset.x,
                                sizeInDp,
                                displayRotation
                            )
                        }
                    else
                        detectTapGestures { offset ->
                            viewModel.updateMeasuredPixel(
                                offset.y,
                                sizeInDp,
                                displayRotation
                            )
                        }

                },
            color = MaterialTheme.colorScheme.background
        ) {
            ScalaBar(
                direction = guessIronState.scalaDirection,
                scalaPosition = ScalaPosition.Left,
                measureOffset = Offset(0F, guessIronState.measuredPixel),
                onMeasuedMMToBig = {
                    if (!showSaveDialog && !showMeasureToEdgeNotSupported)
                        showMeasuredValueToBig = true
                },
                scalaFactor = guessIronState.scalaFactor,
                scalaStartMM = if (guessIronState.scalaOffsetActive) guessIronState.scalaOffset else 0
            )
            ScalaBar(
                direction = guessIronState.scalaDirection,
                scalaPosition = ScalaPosition.Right,
                measureOffset = Offset(0F, guessIronState.measuredPixel),
                scalaFactor = guessIronState.scalaFactor,
                scalaStartMM = if (guessIronState.scalaOffsetActive) guessIronState.scalaOffset else 0
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                ElevatedCard(
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 6.dp
                    )
                ){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {

                        Button(modifier = Modifier.padding( top = 6.dp),onClick = {}) {

                            if (guessIronState.scalaDirection == ScalaDirection.Center) {
                                val centerValue = guessIronState.measuredDistance / 2
                                Text(text = AnnotatedString(text = "$centerValue | ${guessIronState.measuredDistance} ${guessIronState.measuredUnit}"))
                            } else
                                Text(text = AnnotatedString(text = "${
                                    getMeasuredDistance(
                                        guessIronState
                                    )
                                } ${guessIronState.measuredUnit}"))
                        }
                        GuessIronMenu(
                            onClickSaveMeasuredValue = {
                                showSaveDialog = true
                            },
                            onClickNaviIcon = onClickShowMeasured,
                            onShowSetting = onShowSetting
                        )
                    }
                }

                MeasureMenu(
                    scalaDirection = guessIronState.scalaDirection,
                    onClickSwitchScala = {
                        scope.launch {
                            viewModel.switchScala(guessIronState.scalaDirection)
                        }
                    },
                    onClickAddScalaOffset = {

                        if (guessIronState.scalaDirection != ScalaDirection.Center && !guessIronState.scalaOffsetActive && guessIronState.scalaOffset == 0 )
                        {
                            showConfigureDisplayBorder = true
                        }
                        else {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && isLandsacpe) {
                                showMeasureToEdgeNotSupported = true
                            }
                            else {
                                scope.launch {
                                    viewModel.toggleScalaOffset(guessIronState.scalaOffsetActive)
                                }
                            }
                        }
                    },
                    offsetActive = guessIronState.scalaOffsetActive
                )
            }
        }
    }

    SnackbarMeasured(snackbarHostState, onClickShowMeasured)

    if (showConfigureDisplayBorder){
        Dialog(onDismissRequest = {  showConfigureDisplayBorder = false }) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MeasureToEdgeObject()
                    Text(
                        text =  stringResource(R.string.ExplainConfigMargins),
                        modifier = Modifier
                            .wrapContentSize(Alignment.Center),
                        textAlign = TextAlign.Center,
                    )

                    Button(onClick = {
                        showConfigureDisplayBorder = false
                        onConfigDisplayBorder()
                    }) {
                        Text(text = stringResource(R.string.MarginConfigure))
                    }
                }
            }
        }
    }

    if (showMeasureToEdgeNotSupported){
        Dialog(onDismissRequest = {
            showMeasureToEdgeNotSupported = false
            if (guessIronState.scalaOffsetActive)
                scope.launch {
                    viewModel.toggleScalaOffset(true)
                }
        }) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource( R.string.ExplainMeasureToEdgeAndroidVersionNotSupported ),
                        modifier = Modifier
                            .wrapContentSize(Alignment.Center),
                        textAlign = TextAlign.Center,
                    )

                    Button(onClick = {
                        showMeasureToEdgeNotSupported = false
                        if (guessIronState.scalaOffsetActive)
                            scope.launch {
                                viewModel.toggleScalaOffset(true)
                            }
                    }) {
                        Text(text = stringResource(R.string.Ok))
                    }

                }
            }

        }

    }

    if (showMeasuredValueToBig){
        Dialog(onDismissRequest = {
            showMeasuredValueToBig = false
        }) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.MeasuredValueToLong),
                        modifier = Modifier
                            .wrapContentSize(Alignment.Center),
                        textAlign = TextAlign.Center,
                    )

                    Button(onClick = {
                        showMeasuredValueToBig = false
                    }) {
                        Text(text = stringResource(id = R.string.Ok))
                    }

                }
            }

        }

    }

    if (showSaveDialog) {
        val pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val savedTimestamp = LocalDateTime.now().format(pattern)
        val savedMeasuredDistance = getMeasuredDistance(
            guessIronState
        )

        SaveDialog(
            measuredValue = savedMeasuredDistance,
            measuredUnit = guessIronState.measuredUnit,
            defaultName = "",
            onDismissRequest = { showSaveDialog = false }) {

            val newMeasuredItem =
                MeasuredValue.newBuilder()
                    .setTimestamp(savedTimestamp)
                    .setMeasured(savedMeasuredDistance)
                    .setName(it)

            scope.launch {
                viewModel.addMeasuredValue(measuredValue = newMeasuredItem.build())
                snackbarHostState.showSnackbar( message = "", duration = SnackbarDuration.Short )
            }
            showSaveDialog = false
        }
    }
}

private fun getMeasuredDistance(guessIronState: GuessIronUiState) =
    if (guessIronState.scalaOffsetActive && guessIronState.measuredDistance < guessIronState.scalaOffset) guessIronState.scalaOffset else guessIronState.measuredDistance

@Composable
fun DynamicSystemBar(isLandsacpe: Boolean) {
    val activity = LocalContext.current as Activity

    if (isLandsacpe) {
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).let { controller ->
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }
    } else {
        WindowCompat.setDecorFitsSystemWindows(activity.window, true)
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).let { controller ->
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}

@Composable
private fun SnackbarMeasured(
    snackbarHostState: SnackbarHostState,
    onClickShowMeasured: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), Alignment.BottomCenter) {
        SnackbarHost(hostState = snackbarHostState) {
            Snackbar(modifier = Modifier.padding(
                start = 16.dp, end = 16.dp
            ),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                action = {
                    TextButton(onClick = onClickShowMeasured) {
                        Text(text = stringResource(id = R.string.Show))
                    }
                }) {
                Text(text = stringResource(id = R.string.Measured_Value_Saved))
            }
        }
    }
}
