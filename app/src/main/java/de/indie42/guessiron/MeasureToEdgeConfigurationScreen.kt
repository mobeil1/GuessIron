package de.indie42.guessiron

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dock
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayBorderScreen(
    viewModel: GuessIronViewModel = viewModel(),
    onEdit: (ScalaDirection, Int) -> Unit,
    onBack: () -> Unit) {


    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberScrollState()

    val configuration = LocalConfiguration.current
    val isLandsacpe = Configuration.ORIENTATION_LANDSCAPE == configuration.orientation


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                title = {
                    Text(stringResource(id = R.string.ScreenMargin))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.Back)
                        )
                    }
                },
                actions = {
                },
                scrollBehavior = scrollBehavior,
            )
        },

        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(modifier = Modifier
            .padding(innerPadding)
            .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            if (isLandsacpe){
                Row (modifier = Modifier.padding(16.dp),horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    MeasureToEdgeObject()
                    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                        ConfigurationSettings(onEdit, viewModel)
                    }
                }
            }
            else {
                MeasureToEdgeObject()

                ConfigurationSettings(onEdit, viewModel)
            }
        }
    }
}

@Composable
private fun ConfigurationSettings(
    onEdit: (ScalaDirection, Int) -> Unit,
    viewModel: GuessIronViewModel
) {
    val guessIronData by viewModel.dataState.collectAsState()

    Text(
        modifier = Modifier.padding(4.dp),
        text = stringResource(id = R.string.ExplainMeasureToEdge)
    )

    ListItem(
        modifier = Modifier.clickable {
            onEdit(ScalaDirection.Top, guessIronData.displayBorder.top)
        },
        headlineContent = { Text(stringResource(id = R.string.ScreenMarginTop)) },
        leadingContent = {
            Icon(
                imageVector = Icons.Filled.Dock, modifier = Modifier.rotate(180F),
                contentDescription = stringResource(id = R.string.ScreenMarginTop),
            )
        },
        supportingContent = { Text("${guessIronData.displayBorder.top} mm") },
        trailingContent = {
            Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(id = R.string.ScreenMarginTop))
        }
    )
    ListItem(
        modifier = Modifier.clickable {
            onEdit(ScalaDirection.Bottom, guessIronData.displayBorder.bottom)
        },
        headlineContent = { Text(stringResource(id = R.string.ScreenMarginBottom)) },
        leadingContent = {
            Icon(imageVector = Icons.Filled.Dock, contentDescription = stringResource(id = R.string.ScreenMarginBottom))
        },
        supportingContent = { Text("${guessIronData.displayBorder.bottom} mm") },
        trailingContent = {
            Icon(imageVector = Icons.Filled.Edit, contentDescription = stringResource(id = R.string.ScreenMarginBottom))
        }

    )
}

@Composable
fun MeasureToEdgeObject() {

    val infiniteTransition = rememberInfiniteTransition(label = "l1")

    // Creates a child animation of float type as a part of the [InfiniteTransition].
    val scale by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 16F,
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

    Surface(
        modifier = Modifier
            .size(200.dp, 175.dp),
        color = MaterialTheme.colorScheme.background
    ) {
        val primaryContainer = MaterialTheme.colorScheme.primaryContainer
        Box(modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(color = Color.DarkGray, size = Size(200.dp.toPx(), 9.dp.toPx()))
                drawRect(
                    color = Color.DarkGray,
                    topLeft = Offset(x = 75.dp.toPx(), 0F),
                    size = Size(9.dp.toPx(), 150.dp.toPx())
                )
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
                drawCircle(
                    color = Color.Black,
                    radius = 10.dp.toPx(),
                    center = Offset(x = 185.dp.toPx(), y = 30.dp.toPx())
                )

            }
            .padding(top = 10.dp, end = 125.dp)) {
            Box(
                modifier = Modifier
                    .padding(start = 90.dp, top = 50.dp)
                    .size(200.dp, 175.dp)
            ) {
                ScalaBar(
                    ScalaDirection.Top,
                    ScalaPosition.Left,
                    scalaFactor = 1F,
                    measuredMM = scale.toInt(),
                    scalaStartMM = 8,
                    supportLandscapeMode = false
                )
            }
        }
    }
}
