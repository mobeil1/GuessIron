package de.indie42.guessiron

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun SaveDialog(
    measuredValue: String,
    measuredUnit: String,
    defaultName: String = "10.11.23 13:12:00",
    onDismissRequest: () -> Unit,
    onConfirmation: (aName: String) -> Unit,
) {
    var text by remember { mutableStateOf(defaultName) }
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
                    text = stringResource(id = R.string.Save_Measured_Text),
                    modifier = Modifier.padding(16.dp),
                )
                Text(
                    text = "$measuredValue $measuredUnit",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp),
                )
                Row {
                    OutlinedTextField(
                        value = text,
                        modifier = Modifier
                            .padding(16.dp)
                            .focusRequester(focusInput),
                        onValueChange = { text = it },
                        label = { Text(stringResource(id = R.string.Name)) },
                        placeholder = { Text(stringResource(id = R.string.Name_example)) },
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
                        onClick = { onConfirmation(text) },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text(stringResource(id = R.string.Save))
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

@Preview(showBackground = true)
@Composable
fun DialogPreview() {
    //SaveDialog(measuredValue = 123, measuredUnit = "NN", onDismissRequest = {}, onConfirmation = {})

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
            ){

                ExtendedFloatingActionButton(
                    modifier = Modifier.padding(16.dp),
                    onClick = {

                    },
                    icon = {
                        Icon(
                            Icons.Filled.Add,
                            "Extended floating action button."
                        )
                    },
                    text = { Text(text = "123 mm") },
                )
            }
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedIconButton(
                    modifier = Modifier.padding(8.dp),
                    onClick = { },
                ) {
                    Icon(Icons.Filled.BrightnessAuto, "Floating action button.")
                }
                OutlinedIconButton(
                    modifier = Modifier.padding(8.dp),
                    onClick = {  },
                ) {
                    Icon(Icons.Filled.RestartAlt, "Floating action button.")
                }
                OutlinedIconButton(
                    modifier = Modifier.padding( 8.dp),
                    onClick = {  },
                ) {
                    Icon(Icons.Filled.Remove, "Floating action button.")
                }
            }
        }
    }
}