package de.indie42.guessiron

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dock
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
private fun getScalaOffset(guessIronState: GuessIronUiState): Int {

    var scalaOffset = if (guessIronState.scalaOffsetActive) guessIronState.scalaOffset else 0

    if (guessIronState.endlessModeActive)
        scalaOffset += guessIronState.endlessValue

    return scalaOffset
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GuessIronScreen(
    onClickShowMeasured: () -> Unit,
    onShowDisclaimer: () -> Unit,
    onShowCalibration: () -> Unit,
    onConfigDisplayBorder: () -> Unit,
    viewModel: GuessIronViewModel = viewModel(),
) {

    val guessIronState by viewModel.uiState.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var showConfigureDisplayBorder by rememberSaveable { mutableStateOf(false) }
    var showMeasureToEdgeNotSupported by rememberSaveable { mutableStateOf(false) }
    var showMeasuredValueToBig by rememberSaveable { mutableStateOf(false) }
    var showMoreMenu by rememberSaveable { mutableStateOf(false) }
    var showHowToEndless by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    if (!guessIronState.disclaimerDisabled) {
        onShowDisclaimer()
    }

    val configuration = LocalConfiguration.current
    val isLandsacpe = Configuration.ORIENTATION_LANDSCAPE == configuration.orientation


    var displayRotation by remember {
        mutableIntStateOf(android.view.Surface.ROTATION_0)
    }


    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        displayRotation = LocalContext.current.display?.rotation ?: android.view.Surface.ROTATION_0
    } else {
        if (isLandsacpe && guessIronState.scalaOffsetActive)
            showMeasureToEdgeNotSupported = true
    }

    val winInsetsSystembars =
        if (/*Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&*/ guessIronState.scalaOffsetActive) {
            /*val displayRotation = LocalContext.current.display?.rotation ?: 0
            if (displayRotation == android.view.Surface.ROTATION_270)*/
            dynamicSystemBar(true)
        } else
            dynamicSystemBar(isLandsacpe)

    var sizeInDp by remember { mutableStateOf(IntSize.Zero) }

    val scaleOffsetTotal: Int by animateIntAsState(getScalaOffset(guessIronState), label = "a", animationSpec = guessIronState.scalaOffsetAnimation)

    //val acc = rememberSensorState();

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = { }
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {

            // A surface container using the 'background' color from the theme
            Surface(
                modifier = Modifier
                    .windowInsetsPadding(winInsetsSystembars)
                    .fillMaxSize()
                    .onSizeChanged {
                        sizeInDp = it

                        viewModel.setSizeInDp(if (isLandsacpe) it.width else it.height)

                    }
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
                                    displayRotation = displayRotation
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
                    scalaStartMM = scaleOffsetTotal
                )
                ScalaBar(
                    direction = guessIronState.scalaDirection,
                    scalaPosition = ScalaPosition.Right,
                    measureOffset = Offset(0F, guessIronState.measuredPixel),
                    scalaFactor = guessIronState.scalaFactor,
                    scalaStartMM = scaleOffsetTotal
                )
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                if (isLandsacpe){
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ){
                        MainMenu(
                            guessIronState,
                            scope,
                            viewModel,
                            true,
                            showConfigureDisplayBorder = {
                                showConfigureDisplayBorder = true
                            },
                            showMeasureToEdgeNotSupported = {
                                showMeasureToEdgeNotSupported = true
                            },
                            showHowToEndless = {if (!guessIronState.endlessMeasureHowToDisabled)
                                showHowToEndless = !guessIronState.endlessModeActive
                            },
                            showMoreMenu = { showMoreMenu = true }
                        )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ){
                            MeasureEnlessMenu(guessIronState, scope, viewModel, guessIronState.sizeInMM, /*acc,*/ isLandsacpe = true)
                        }

                    }
                }
                else {
                    MainMenu(
                        guessIronState,
                        scope,
                        viewModel,
                        false,
                        showConfigureDisplayBorder = {
                            showConfigureDisplayBorder = true
                        },
                        showMeasureToEdgeNotSupported = {
                            showMeasureToEdgeNotSupported = true
                        },
                        showHowToEndless = {
                            if (!guessIronState.endlessMeasureHowToDisabled)
                                showHowToEndless = !guessIronState.endlessModeActive
                        },
                        showMoreMenu = { showMoreMenu = true }
                    )
                    MeasureEnlessMenu(guessIronState, scope, viewModel, guessIronState.sizeInMM, /*acc,*/ isLandsacpe = false)
                }
            }
        }

        if (showHowToEndless) {
            HowToEndless(guessIronState.sizeInMM, onDismiss = { showHowToEndless = false }, onDisableHowTo = {
                scope.launch { viewModel.disableEndlessMeasureHowTo() }
            }, isLandsacpe = isLandsacpe)
        }

        if (showMoreMenu) {
            MoreMenu(
                onDismiss = { showMoreMenu = false },
                onShowSaveDialog = {
                    showMoreMenu = false
                    showSaveDialog = true
                },
                onClickShowMeasured = {
                    showMoreMenu = false
                    onClickShowMeasured()
                },
                onShowCalibration = {
                    showMoreMenu = false
                    onShowCalibration()
                                    },
                onShowScreenBorder = {
                    showMoreMenu = false
                    onConfigDisplayBorder()
                }
            )
        }

        SnackbarMeasured(snackbarHostState, onClickShowMeasured)

        if (showConfigureDisplayBorder) {
            Dialog(onDismissRequest = { showConfigureDisplayBorder = false }) {

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
                            text = stringResource(R.string.ExplainConfigMargins),
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

        if (showMeasureToEdgeNotSupported) {
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
                            text = stringResource(R.string.ExplainMeasureToEdgeAndroidVersionNotSupported),
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

        if (showMeasuredValueToBig) {
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
                        .setMeasureType( if (guessIronState.endlessValue > 0 ) MeasuredValue.MeasureType.Endless else MeasuredValue.MeasureType.Single)

                scope.launch {
                    viewModel.addMeasuredValue(measuredValue = newMeasuredItem.build())
                    snackbarHostState.showSnackbar(message = "", duration = SnackbarDuration.Short)
                }
                showSaveDialog = false
            }
        }
    }
}

@Composable
private fun MainMenu(
    guessIronState: GuessIronUiState,
    scope: CoroutineScope,
    viewModel: GuessIronViewModel,
    isLandsacpe: Boolean,
    showConfigureDisplayBorder: () -> Unit,
    showMeasureToEdgeNotSupported: () -> Unit,
    showHowToEndless: () -> Unit,
    showMoreMenu: () -> Unit
) {
    Button(modifier = Modifier.padding(top = 6.dp), onClick = {}) {

        if (guessIronState.scalaDirection == ScalaDirection.Center) {
            val centerValue = guessIronState.measuredDistance / 2
            Text(text = AnnotatedString(text = "$centerValue | ${guessIronState.measuredDistance} ${guessIronState.measuredUnit}"))
        } else
            Text(
                text = AnnotatedString(
                    text = "${getMeasuredDistance(guessIronState)} ${guessIronState.measuredUnit}"
                )
            )
    }
    MeasureMenu(
        scalaDirection = guessIronState.scalaDirection,
        onClickSwitchScala = {
            scope.launch {
                viewModel.switchScala(guessIronState.scalaDirection)
            }
        },
        onClickAddScalaOffset = {

            if (guessIronState.scalaDirection != ScalaDirection.Center && !guessIronState.scalaOffsetActive && guessIronState.scalaOffset == 0) {
                showConfigureDisplayBorder()
            } else {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R && isLandsacpe) {
                    showMeasureToEdgeNotSupported()
                } else {
                    scope.launch {
                        viewModel.toggleScalaOffset(guessIronState.scalaOffsetActive)
                    }
                }
            }
        },
        onMeasureEndless = {
            scope.launch {

                showHowToEndless()

                viewModel.toggleEndlessMode(guessIronState.endlessModeActive)
            }
        },
        onMore = showMoreMenu,
        offsetActive = guessIronState.scalaOffsetActive,
        measureEndlessActive = guessIronState.endlessModeActive
    )
}

@Composable
private fun ColumnScope.MeasureEnlessMenu(
    guessIronState: GuessIronUiState,
    scope: CoroutineScope,
    viewModel: GuessIronViewModel,
    heightInMM: Int,
    /*acc: EndlessAutomaticState,*/
    isLandsacpe: Boolean
) {

    val aniEnterDirection = if ( isLandsacpe ) fadeIn() + expandHorizontally() else fadeIn() + expandVertically()
    val aniExitDirection = if ( isLandsacpe ) fadeOut() + shrinkHorizontally() else fadeOut() + shrinkVertically()

    AnimatedVisibility(guessIronState.endlessModeActive, enter = aniEnterDirection, exit = aniExitDirection) {
        ElevatedCard(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    ExtendedFloatingActionButton(
                        modifier = Modifier.padding(
                            start = 8.dp,
                            top = 8.dp,
                            end = 8.dp
                        ),
                        onClick = {
                            scope.launch {
                                viewModel.setMeasuredEndlessStepValue(heightInMM + guessIronState.endlessValue)
                            }
                        },
                        icon = {
                            Icon(
                                Icons.Filled.Add,
                                "Extended floating action button."
                            )
                        },
                        text = { Text(text = "$heightInMM mm") },
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                    IconButton(
//                        modifier = Modifier.padding(start = 8.dp),
//                        colors = if (acc.active.value) IconButtonDefaults.iconButtonColors(
//                            containerColor = MaterialTheme.colorScheme.primaryContainer,
//                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                        ) else IconButtonDefaults.iconButtonColors(),
//                        onClick = { acc.activatedAutomatic(!acc.active.value) },
//                    ) {
//                        Icon(Icons.Filled.BrightnessAuto, "Floating action button.")
//                    }
                    IconButton(
                        modifier = Modifier.padding(8.dp),
                        onClick = { viewModel.setMeasuredEndlessStepValue(0) },
                    ) {
                        Icon(Icons.Filled.RestartAlt, "Floating action button.")
                    }
                    IconButton(
                        modifier = Modifier.padding(end = 8.dp),
                        onClick = { viewModel.setMeasuredEndlessStepValue(guessIronState.endlessValue - heightInMM) },
                    ) {
                        Icon(Icons.Filled.Remove, "Floating action button.")
                    }
                }
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun MoreMenuPreview() {
    HowToEndless(heightInMM = 456, onDismiss = {}, onDisableHowTo = {}, isLandsacpe = true)
}

@Composable
fun HowToEndless(heightInMM: Int, onDismiss: () -> Unit, onDisableHowTo: () -> Unit, isLandsacpe: Boolean) {

    val (checkedState, onStateChange) = remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                if (isLandsacpe){
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        MeasureEndlessAnimation(heightInMM = heightInMM)
                        Column(
                            modifier = Modifier,
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            MeasureEndlessText(
                                checkedState,
                                onStateChange,
                                onDisableHowTo,
                                onDismiss
                            )
                        }
                    }
                }
                else{
                    MeasureEndlessAnimation(heightInMM = heightInMM)

                    MeasureEndlessText(checkedState, onStateChange, onDisableHowTo, onDismiss)
                }

            }
        }
    }
}

@Composable
private fun MeasureEndlessText(
    checkedState: Boolean,
    onStateChange: (Boolean) -> Unit,
    onDisableHowTo: () -> Unit,
    onDismiss: () -> Unit
) {
    Text(
        modifier = Modifier.padding(8.dp),
        text = stringResource(id = R.string.HowToMeasureEndless),
        style = MaterialTheme.typography.bodyLarge
    )

    Row(
        Modifier
            .fillMaxWidth()
            .toggleable(
                value = checkedState,
                onValueChange = { onStateChange(!checkedState) },
                role = Role.Checkbox
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checkedState,
            onCheckedChange = null // null recommended for accessibility with screenreaders
        )
        Text(
            text = stringResource(id = R.string.DoNotShowAgain),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
    Button(
        modifier = Modifier.padding(bottom = 8.dp),
        onClick = {
            if (checkedState)
                onDisableHowTo()

            onDismiss()
        }) {
        Text(text = stringResource(id = R.string.Ok))
    }
}

@Composable
fun MeasureEndlessAnimation(heightInMM: Int) {

    val infiniteTransition = rememberInfiniteTransition(label = "l1")

    val iconPosition by infiniteTransition.animateValue(
        initialValue = 0.dp,
        targetValue = 90.dp,
        typeConverter = Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                0.dp at 0
                30.dp at 400
                30.dp at 1000 // ms
                60.dp at 1400 // ms
                60.dp at 2000
                90.dp at 2400
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .size(130.dp, 225.dp)
            .padding(top = 8.dp, start = 6.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        val iconStoppedPosition = (iconPosition > 28.dp && iconPosition < 32.dp) || (iconPosition > 58.dp && iconPosition < 62.dp) ||
                (iconPosition > 88.dp && iconPosition < 92.dp)
        val primaryContainer =
            if (iconStoppedPosition) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground

        val repeatText = if (iconPosition > 28.dp && iconPosition < 32.dp)
            (heightInMM*1).toString()+" mm"
        else if (iconPosition > 58.dp && iconPosition < 62.dp)
            (heightInMM*2).toString()+" mm"
        else if (iconPosition > 88.dp && iconPosition < 92.dp)
            (heightInMM*3).toString()+" mm"
        else
            ""

        Box(modifier = Modifier
            .size(80.dp, 175.dp)
            .drawBehind {
                drawRect(color = Color.DarkGray, size = Size(75.dp.toPx(), 9.dp.toPx()))
                drawRect(
                    color = Color.DarkGray,
                    topLeft = Offset(x = 20.dp.toPx(), 0F),
                    size = Size(9.dp.toPx(), 150.dp.toPx())
                )
            }
            .padding(top = 10.dp)) {

            Box(
                modifier = Modifier
                    .padding(start = 30.dp, top = iconPosition)
                    .fillMaxSize()
            )
            {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.scale(1.2F),
                        tint = primaryContainer,
                        imageVector = Icons.Filled.Smartphone,
                        contentDescription = ""
                    )
                    Text(
                        modifier = Modifier.padding(6.dp),
                        color = primaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                        text = repeatText

                    )
                }

            }
        }
        Column( verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally){
        ExtendedFloatingActionButton(
            modifier = Modifier
                .scale(if (iconStoppedPosition) 1F else 0.8F)
                .padding(
                    bottom = 6.dp,
                ),
            onClick = {

            },
            icon = {
                Icon(
                    Icons.Filled.Add,
                    "Extended floating action button."
                )
            },
            text = { Text( text = "$heightInMM mm", style = MaterialTheme.typography.bodySmall) },
        )
        }
    }
}


@Composable
private fun MoreMenu(
    onDismiss: () -> Unit,
    onShowSaveDialog: () -> Unit,
    onClickShowMeasured: () -> Unit,
    onShowCalibration: () -> Unit,
    onShowScreenBorder: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row {Text(text = stringResource(id = R.string.measured), color = MaterialTheme.colorScheme.onPrimaryContainer) }
                MoreMenuItem(onClickable = onShowSaveDialog, icon = Icons.Filled.Save, text = stringResource(id = R.string.Save))
                MoreMenuItem(onClickable = onClickShowMeasured, icon = Icons.AutoMirrored.Filled.List, text = stringResource(id = R.string.Show))

                Row {Text(text = stringResource(id = R.string.settings), color = MaterialTheme.colorScheme.onPrimaryContainer) }
                MoreMenuItem(onClickable = onShowCalibration, icon = Icons.Filled.ZoomOutMap, text = stringResource(id = R.string.calibration))
                MoreMenuItem(onClickable = onShowScreenBorder, icon = Icons.Filled.Dock, text = stringResource(id = R.string.ScreenMargin))
            }
        }
    }
}

@Composable
fun MoreMenuItem( onClickable: () -> Unit, icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clickable { onClickable() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, modifier = Modifier.rotate(180F),
            contentDescription = stringResource(id = R.string.ScreenMargin),
        )

        Text(
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodyLarge,
            text = text
        )
    }
}

private fun getMeasuredDistance(guessIronState: GuessIronUiState) =
    if (guessIronState.scalaOffsetActive && guessIronState.measuredDistance < guessIronState.scalaOffset)
        guessIronState.scalaOffset + guessIronState.endlessValue
    else guessIronState.measuredDistance + guessIronState.endlessValue

@Composable
fun dynamicSystemBar(isLandsacpe: Boolean): WindowInsets {

    val activity = LocalContext.current as Activity

    return if (isLandsacpe) {
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).let { controller ->
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
        WindowInsets.statusBars
    } else {
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).let { controller ->
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
        WindowInsets.systemBars
    }
}

@Composable
private fun SnackbarMeasured(
    snackbarHostState: SnackbarHostState,
    onClickShowMeasured: () -> Unit
) {
    Box(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize(), Alignment.BottomCenter
    ) {
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
