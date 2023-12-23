package de.indie42.guessiron

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@Composable
fun NavigationMenu(
    scalaDirection: ScalaDirection,
    onClickSwitchScala: () -> Unit,
    onClickSaveMeasuredValue: () -> Unit,
    onClickNaviIcon: () -> Unit,
    onStartCalibration: () -> Unit
) {

    val directionIcon = when(scalaDirection){
        ScalaDirection.Top -> Icons.Filled.VerticalAlignTop
        ScalaDirection.Bottom -> Icons.Filled.VerticalAlignBottom
        ScalaDirection.Center -> Icons.Filled.VerticalAlignCenter
    }

    Row(horizontalArrangement = Arrangement.SpaceEvenly) {
        IconButton(onClick = onClickSwitchScala) {
            Icon(directionIcon, contentDescription = stringResource(id = R.string.SwitchScala))
        }
        IconButton(onClick = onStartCalibration) {
            Icon(Icons.Filled.ZoomOutMap, contentDescription = stringResource(id = R.string.StartCalibration))
        }
        IconButton(onClick = onClickSaveMeasuredValue) {
            Icon(Icons.Filled.Save, contentDescription = stringResource(id = R.string.SaveMeasuredValue))
        }
        IconButton(onClick = onClickNaviIcon) {
            Icon(Icons.AutoMirrored.Filled.List, contentDescription = stringResource(id = R.string.ShowMeasuredValues) )
        }
    }
}
