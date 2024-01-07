package de.indie42.guessiron

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScalaCalibrationOverviewScreen(
    viewModel: GuessIronViewModel,
    onStartCalibration: (mode: Int) -> Unit,
    onBack: () -> Unit
) {

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
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(text = AnnotatedString(text = stringResource(id = R.string.AskHowToCalibrate)))

            OutlinedButton(onClick = {
                onStartCalibration(ScalaCalibrationMode.Ruler.value)
            }) {
                Text(text = AnnotatedString(text = stringResource(id = R.string.WithARuler)))
            }
            OutlinedButton(onClick = {
                onStartCalibration(ScalaCalibrationMode.Card.value)
            }) {
                Text(text = AnnotatedString(text = stringResource(id = R.string.WithACreditcard)))
            }
            OutlinedButton(onClick = {
                onStartCalibration(ScalaCalibrationMode.UserDef.value)
            }) {
                Text(text = AnnotatedString(text = stringResource(id = R.string.WithAnotherItem)))
            }
            FilledTonalButton(onClick = {
                scope.launch {
                    viewModel.changeScalaFactor(1.0F)
                }
                onBack()
            }) {
                Text(text = AnnotatedString(text = stringResource(id = R.string.ResetCalibration)))
            }

        }
    }
}
