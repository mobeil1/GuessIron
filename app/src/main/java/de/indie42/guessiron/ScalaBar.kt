package de.indie42.guessiron

import android.content.res.Configuration
import android.os.Build
import android.view.Surface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

enum class ScalaDirection ( val value: Int ) {
    Top(0), Bottom(1), Center(2)
}

enum class ScalaPosition {
    Left, Right
}


@Composable
fun ScalaBar(
    direction: ScalaDirection = ScalaDirection.Top,
    scalaPosition: ScalaPosition = ScalaPosition.Right,
    scalaColor: Color = MaterialTheme.colorScheme.onBackground,
    measureOffset: Offset = Offset(0f, 0f),
    measuredMM: Int = 0,
    onMeasuedMMToBig: () -> Unit = {},
    scalaStartMM: Int = 0,
    scalaFactor: Float = 1F,
    supportLandscapeMode: Boolean = true,
    ) {

    val configuration = LocalConfiguration.current
    val isLandsacpe = Configuration.ORIENTATION_LANDSCAPE == configuration.orientation && supportLandscapeMode

    var rotationDirection = direction

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && supportLandscapeMode) {
        val displayRotation = LocalContext.current.display?.rotation ?: 0
        if (displayRotation == Surface.ROTATION_270 || displayRotation == Surface.ROTATION_180)
            rotationDirection = when (direction) {
                ScalaDirection.Top -> ScalaDirection.Bottom
                ScalaDirection.Bottom -> ScalaDirection.Top
                else -> direction
            }
    }

    val scalaCalculator = ScalaCalculator(rotationDirection, scalaPosition, scalaFactor, scalaStartMM, isLandsacpe)

    var measuredOffset = measureOffset
    if (measuredMM > 0)
        measuredOffset = Offset( x = 0F, y = scalaCalculator.getMMinPixel(measuredMM))

    ScalaLines(
        measureOffset = measuredOffset,
        scalaColor = scalaColor,
        scalaCalculator = scalaCalculator,
        onMeasuedMMToBig
    )

    ScalaNumber(
        scalaCalculator = scalaCalculator,
        scalaColor = scalaColor
    )
}

@Composable
fun ScalaLines(measureOffset: Offset, scalaColor: Color, scalaCalculator: ScalaCalculator, onMeasuedMMToBig: () -> Unit) {

    val measureColor = MaterialTheme.colorScheme.primary
    val measureOnColor = MaterialTheme.colorScheme.onPrimary

    val dpmm = scalaCalculator.dpmm

    Canvas(modifier = Modifier.fillMaxSize()) {

        val lineStartX = scalaCalculator.getScalaStartX(size)

        var startMeasured = measureOffset.y

        val scaleLengthInPixel = scalaCalculator.getScaleLengthInPixel(size)

        val threshold = scalaCalculator.getMMinPixel(1)

        if (scaleLengthInPixel + threshold < measureOffset.y)
            onMeasuedMMToBig()

        if (scalaCalculator.scalaDirection == ScalaDirection.Center) {
            val centerMeasured = measureOffset.y / 2F
            val centerScreen = scaleLengthInPixel / 2F
            startMeasured = centerScreen - centerMeasured // - scalaCalculator.directionOffset(size).y
        }

        // Messdaten anzeigen
        val measuredSizeLeft = if ( scalaCalculator.isLandsacpe )
                Size( measureOffset.y, dpmm * 8)
            else
                Size(dpmm * 8, measureOffset.y)

        val measuredSizeRight = if ( scalaCalculator.isLandsacpe )
            Size(measureOffset.y, dpmm * 8 * -1)
        else
            Size(dpmm * 8 * -1, measureOffset.y)

        if (scalaCalculator.scalaDirection == ScalaDirection.Bottom) {

            val topLeft = if (scalaCalculator.isLandsacpe)
                Offset(x = scaleLengthInPixel - measureOffset.y, y = lineStartX )
            else
                Offset(x = lineStartX, y = scaleLengthInPixel - measureOffset.y)

            if (scalaCalculator.scalaPosition == ScalaPosition.Left)
                drawRect(
                    measureColor,
                    topLeft,
                    size = measuredSizeLeft
                )
            else
                drawRect(
                    measureColor,
                    topLeft,
                    size = measuredSizeRight
                )
        } else {
            val topLeftCenter = if (scalaCalculator.isLandsacpe)
                Offset(x = startMeasured, y = lineStartX)
            else
                Offset(x = lineStartX, y = startMeasured)

            val topLeft = if (scalaCalculator.isLandsacpe)
                Offset(x = 0F, y = lineStartX)
            else
                Offset(x = lineStartX, y = 0f)

            if (scalaCalculator.scalaPosition == ScalaPosition.Left) {
                if (scalaCalculator.scalaDirection == ScalaDirection.Center) {
                    drawRect(
                        measureColor,
                        topLeftCenter,
                        size = measuredSizeLeft
                    )
                } else
                    drawRect(
                        measureColor,
                        topLeft,
                        size = measuredSizeLeft
                    )
            } else
            {
                if (scalaCalculator.scalaDirection == ScalaDirection.Center) {
                    drawRect(
                        measureColor,
                        topLeftCenter,
                        size = measuredSizeRight
                    )
                } else
                    drawRect(
                        measureColor,
                        topLeft,
                        size = measuredSizeRight
                    )
            }

        }

        for (i in 0 until scalaCalculator.getHeightInMM(size)) {

            val newLineStart = scalaCalculator.getScalaLineOffsetStart(i, size)

            val newLineEnd = scalaCalculator.getScalaLineOffsetEnd(lineCounter = i, drawSize = size)

            val linePosInPixel = if (scalaCalculator.isLandsacpe)
                    newLineStart.x
                else
                    newLineStart.y

            var lineColor = measureOnColor
            if (scalaCalculator.scalaDirection == ScalaDirection.Bottom) {
                lineColor = scalaColor
                if (linePosInPixel >= scaleLengthInPixel- measureOffset.y) {
                    lineColor = measureOnColor
                }
            } else if (scalaCalculator.scalaDirection == ScalaDirection.Center) {

                if (linePosInPixel < startMeasured || linePosInPixel >= startMeasured+measureOffset.y)
                    lineColor = scalaColor

            } else if (linePosInPixel >= measureOffset.y) {
                lineColor = scalaColor
            }

            drawLine(
                start = newLineStart,
                end = newLineEnd,
                color = lineColor,
                strokeWidth = scalaCalculator.getStrokeWidth(i, size)
            )
        }
    }

}

@Composable
fun ScalaNumber(scalaCalculator: ScalaCalculator, scalaColor: Color) {

    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.fillMaxSize()) {

        for (i in 0 until scalaCalculator.getHeightInMM(size)) {

            val absolutePosition = scalaCalculator.getLinePosition(i, size)

           if (absolutePosition % 10 == 0 && (absolutePosition > 0 || scalaCalculator.scalaDirection == ScalaDirection.Center)) {

               val lineEndPoint = scalaCalculator.getScalaLineOffsetEnd(i, size)

                val measuredText = textMeasurer.measure(
                    AnnotatedString((absolutePosition / 10).toString()),
                    //style = TextStyle(fontSize = 18.sp)
                )

                val heightOffset = measuredText.size.height / 2F

                val transRotateOffset = 15.dp.toPx()

                var textOffsetFromLineEnd = 0F
                if (absolutePosition == 0)
                    textOffsetFromLineEnd = 25F

                var textX = scalaCalculator.dpmm * 8
                if (scalaCalculator.scalaPosition == ScalaPosition.Right)
                    textX = size.width - textX - measuredText.size.width - transRotateOffset + (textOffsetFromLineEnd * -1)
                else
                    textX += transRotateOffset + textOffsetFromLineEnd

               var textY = lineEndPoint.y

               if (scalaCalculator.isLandsacpe){

                   textX = lineEndPoint.x - measuredText.size.width/2

                   textY = scalaCalculator.getLineLength(i, size) * scalaCalculator.dpmm
                   if (scalaCalculator.scalaPosition == ScalaPosition.Right) {
                       textY = size.height - textY - measuredText.size.height
                   } else {
                       textY += transRotateOffset + textOffsetFromLineEnd
                   }
               }

               val textOffset = Offset( x = textX, y = textY)

                withTransform({
                    translate(top = -heightOffset)
                }) {
                    drawText(
                        measuredText,
                        topLeft = textOffset,
                        color = scalaColor )
                    }
            }
        }
    }
}