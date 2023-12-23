package de.indie42.guessiron

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun DisclaimerScreen(viewModel: GuessIronViewModel, onStartCalibration: () -> Unit, onBack: () -> Unit) {

    val scope = rememberCoroutineScope()

    // Creates an [InfiniteTransition] instance for managing child animations.
    val infiniteTransition = rememberInfiniteTransition(label = "l1")

    // Creates a child animation of float type as a part of the [InfiniteTransition].
    val scale by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 240f,
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
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )  {


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
                        ScalaBar(scalaPosition = ScalaPosition.Left, measureOffset = Offset( x= 0F, y = scale), scalaFactor = 1F)
                    }
                }
            }
            Text(
                text = stringResource(id = R.string.hello),
                style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(id = R.string.disclaimer_info))

            Spacer(modifier = Modifier.padding(16.dp))

            Text(text = stringResource(id = R.string.calibration),
                style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(id = R.string.calibration_info))

            Spacer(modifier = Modifier.padding(8.dp))

            Button(onClick = {
                scope.launch {
                    viewModel.disableDisclaimer()
                    onStartCalibration()
                }
            }) {
                Icon(Icons.Filled.ZoomOutMap, contentDescription = stringResource(id = R.string.StartCalibration))
                Text(modifier = Modifier.padding(6.dp), text = stringResource(id = R.string.calibration))
            }
            Spacer(modifier = Modifier.padding(16.dp))
            OutlinedButton(onClick = {
                scope.launch {
                    viewModel.disableDisclaimer()
                    onBack()
                }
            }) {
                Text(text = stringResource(id = R.string.continue_without_calibration))
            }

        }
    }
}

@Preview(showBackground = true)
@Composable
fun DisclaimerScreenPreview() {

    val scope = rememberCoroutineScope()

    // Creates an [InfiniteTransition] instance for managing child animations.
    val infiniteTransition = rememberInfiniteTransition(label = "l1")

    // Creates a child animation of float type as a part of the [InfiniteTransition].
    val scale by infiniteTransition.animateFloat(
        initialValue = 0F,
        targetValue = 240f,
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
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        )  {


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
                        ScalaBar(scalaPosition = ScalaPosition.Left, measureOffset = Offset( x= 0F, y = scale), scalaFactor = 1F)
                    }
                }
            }
            Text(
                text = stringResource(id = R.string.hello),
                style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(id = R.string.disclaimer_info))

            Spacer(modifier = Modifier.padding(16.dp))

            Text(text = stringResource(id = R.string.calibration),
                style = MaterialTheme.typography.headlineMedium)
            Text(text = stringResource(id = R.string.calibration_info))

            Spacer(modifier = Modifier.padding(8.dp))

            Button(onClick = {
//                scope.launch {
//                    viewModel.disableDisclaimer()
//                    onStartCalibration()
//                }
            }) {
                Icon(Icons.Filled.ZoomOutMap, contentDescription = stringResource(id = R.string.StartCalibration))
                Text(modifier = Modifier.padding(6.dp), text = stringResource(id = R.string.calibration))
            }
            Spacer(modifier = Modifier.padding(16.dp))
            OutlinedButton(onClick = {
//                scope.launch {
//                    viewModel.disableDisclaimer()
//                    onBack()
//                }
            }) {
                Text(text = stringResource(id = R.string.continue_without_calibration))
            }

        }
    }
}