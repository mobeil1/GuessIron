package de.indie42.guessiron

import android.os.Build
import android.view.Surface
import android.view.View
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun MeasureMenu(
    scalaDirection: ScalaDirection,
    onClickSwitchScala: () -> Unit,
    onClickAddScalaOffset: () -> Unit,
    onMeasureEndless: () -> Unit,
    onMore: () -> Unit,
    offsetActive: Boolean = false,
    measureEndlessActive: Boolean = false
) {

    var iconRotation = 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        when (LocalContext.current.display?.rotation ?: 0) {
            Surface.ROTATION_90 -> iconRotation = -90
            Surface.ROTATION_270 -> iconRotation = 90
            Surface.ROTATION_180 -> iconRotation = 180
        }

        if (LocalConfiguration.current.layoutDirection == View.LAYOUT_DIRECTION_RTL)
            iconRotation *= -1
    }


    val directionIcon = when(scalaDirection){
        ScalaDirection.Top -> Icons.Filled.VerticalAlignTop
        ScalaDirection.Bottom -> Icons.Filled.VerticalAlignBottom
        ScalaDirection.Center -> Icons.Filled.VerticalAlignCenter
    }

    val measureToEdgeIcon = when(scalaDirection){
        ScalaDirection.Center -> Icons.Filled.Smartphone
        else -> Icons.Filled.Dock
    }

    val upsideDownRotation = when(scalaDirection){
        ScalaDirection.Bottom -> 180F
        else -> 0F
    }

    val activeColor = IconButtonDefaults.iconButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
    val defaultColor = IconButtonDefaults.iconButtonColors()

    Row( modifier = Modifier.padding(8.dp),horizontalArrangement = Arrangement.SpaceEvenly) {

        IconButton( modifier = Modifier.rotate(iconRotation.toFloat()), onClick = onClickSwitchScala) {
            Icon(directionIcon, contentDescription = stringResource(id = R.string.SwitchScala))
        }
        IconButton( modifier = Modifier.rotate(iconRotation.toFloat()+upsideDownRotation), colors = if (offsetActive) activeColor else defaultColor, onClick = onClickAddScalaOffset) {
            Icon(imageVector = measureToEdgeIcon, modifier = Modifier.rotate(180F), contentDescription = stringResource(id = R.string.SwitchScala))
        }
        IconButton( colors = if (measureEndlessActive) activeColor else defaultColor, onClick = onMeasureEndless) {
            Icon(imageVector = Icons.Filled.Repeat, modifier = Modifier.rotate(180F), contentDescription = stringResource(id = R.string.SwitchScala))
        }
        IconButton( onClick = onMore) {
            Icon(imageVector = Icons.Filled.MoreHoriz, contentDescription = stringResource(id = R.string.settings))
        }
    }
}
