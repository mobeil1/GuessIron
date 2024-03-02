@file:OptIn(ExperimentalMaterial3Api::class)

package de.indie42.guessiron

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutomaticSettingScreen(viewModel: GuessIronViewModel, onBack: () -> Unit) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val scrollState = rememberScrollState()

    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                title = {
                    Text(stringResource(id = R.string.AutomaticSetting))
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(scrollState),
        ) {
            MeasureEndlessAutomaticAnimation()
            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(id = R.string.EndlessAutomaticInfo)
            )
            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(id = R.string.EndlessAutomaticInfoTurnOff),
                style = MaterialTheme.typography.bodyLarge
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(id = R.string.Sensitivity), style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(id = R.string.SensitivityInfo))
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Slider(
                        value = uiState.endlessAutomaticSensitivity,
                        valueRange = 0.3F..0.7F,
                        onValueChange = {
                            scope.launch {
                                viewModel.changeAutomaticSensitivity(it)
                            }
                        },
                        steps = 1,

                        )
                    Text(
                        text = when (uiState.endlessAutomaticSensitivity) {
                            0.3F -> stringResource(id = R.string.High)
                            0.5F -> stringResource(id = R.string.Middle)
                            0.7F -> stringResource(id = R.string.Low)
                            else -> uiState.endlessAutomaticSensitivity.toString()
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Text(text = stringResource(id = R.string.SettlingTime), style = MaterialTheme.typography.titleMedium)
                Text(text = stringResource(id = R.string.SettlingTimeInfo))
                Column(
                    modifier = Modifier.padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    Slider(
                        value = uiState.endlessAutomaticSettlingTime.toFloat(),
                        valueRange = 500.0F..5000.0F,
                        steps = 8,
                        onValueChange = {
                            scope.launch {
                                viewModel.changeAutomaticSettlingTime(it.toInt())
                            }
                        },
                    )
                    Text(
                        text = (uiState.endlessAutomaticSettlingTime / 1000F).toString() + " " + stringResource(
                            id = R.string.Seconds
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

        }
    }
}
