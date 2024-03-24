package de.indie42.guessiron

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasureButtonConfigurationScreen(viewModel: GuessIronViewModel, onBack: () -> Unit) {

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val scope = rememberCoroutineScope()

    val guessIronState by viewModel.uiState.collectAsState()

    val radioOptions = mapOf(
        MeasureButtonFunction.SAVE to stringResource(id = R.string.SaveMeasuredValue),
        MeasureButtonFunction.RESET to stringResource(id = R.string.ResetToZero)
    )

    val (selectedOption, onOptionSelected) = remember { mutableStateOf(guessIronState.measuerButtonFunction) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                title = {
                    Text(stringResource(id = R.string.FunctionMeasureButtton))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.Back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },

        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->


        Column( modifier = Modifier.padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = {}) {
                    Text(text = "123 " + guessIronState.unitSystem.getUnit())
                }
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(id = R.string.FuctionMeasureButtonInfo)
                )
            }
            Column(modifier = Modifier.selectableGroup()) {
                radioOptions.forEach { buttonFunction ->
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .selectable(
                                selected = (buttonFunction.key == selectedOption),
                                onClick = {
                                    onOptionSelected(buttonFunction.key)
                                    scope.launch {
                                        viewModel.changeMeasureButtonFunction(buttonFunction.key)
                                    }
                                },
                                role = Role.RadioButton
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (buttonFunction.key == selectedOption),
                            onClick = null // null recommended for accessibility with screenreaders
                        )
                        Text(
                            text = buttonFunction.value,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        }
    }
}