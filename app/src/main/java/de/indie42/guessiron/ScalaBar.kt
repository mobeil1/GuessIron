package de.indie42.guessiron

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
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
    scalaFactor: Float = 1F,

    ) {

    val scalaCalculator = ScalaCalculator(direction, scalaPosition, scalaFactor)

    var measuredOffset = measureOffset
    if (measuredMM > 0)
        measuredOffset = Offset( x = 0F, y = scalaCalculator.getMMinPixel(measuredMM))

    ScalaLines(
        measureOffset = measuredOffset,
        scalaColor = scalaColor,
        scalaCalculator = scalaCalculator
    )

    ScalaNumber(
        scalaCalculator = scalaCalculator,
        scalaColor = scalaColor
    )
}

@Composable
fun ScalaLines(measureOffset: Offset, scalaColor: Color, scalaCalculator: ScalaCalculator) {

    val measureColor = MaterialTheme.colorScheme.primary
    val measureOnColor = MaterialTheme.colorScheme.onPrimary

    val dpmm = scalaCalculator.dpmm

    //val isLandscape = Configuration.ORIENTATION_LANDSCAPE == Resources.getSystem().configuration.orientation

    Canvas(modifier = Modifier.fillMaxSize()) {

        val lineStartX = scalaCalculator.getScalaStartX(size)

        var startMeasured = measureOffset.y
        //if (scalaCalculator.scalaDirection == ScalaDirection.Bottom)
        //    startMeasured = size.height - startMeasured
        if (scalaCalculator.scalaDirection == ScalaDirection.Center) {
            val centerMeasured = measureOffset.y / 2F
            val centerScreen = size.height / 2F
            startMeasured = centerScreen - centerMeasured // - scalaCalculator.directionOffset(size).y
        }

        // Messdaten anzeigen
        if (scalaCalculator.scalaDirection == ScalaDirection.Bottom) {
            if (scalaCalculator.scalaPosition == ScalaPosition.Left)
                drawRect(
                    measureColor,
                    Offset(x = lineStartX, y = size.height-measureOffset.y),
                    size = Size(dpmm * 8, measureOffset.y)
                )
            else
                drawRect(
                    measureColor,
                    Offset(x = lineStartX, y = size.height-measureOffset.y),
                    size = Size(dpmm * 8 * -1, measureOffset.y)
                )
        } else {
            if (scalaCalculator.scalaPosition == ScalaPosition.Left) {
                if (scalaCalculator.scalaDirection == ScalaDirection.Center) {
                    drawRect(
                        measureColor,
                        Offset(x = lineStartX, y = startMeasured),
                        size = Size(dpmm * 8, measureOffset.y)
                    )
                } else
                    drawRect(
                        measureColor,
                        Offset(x = lineStartX, y = 0f),
                        size = Size(dpmm * 8, measureOffset.y)
                    )
            } else
            {
                if (scalaCalculator.scalaDirection == ScalaDirection.Center) {
                    drawRect(
                        measureColor,
                        Offset(x = lineStartX, y = startMeasured),
                        size = Size(dpmm * 8 * -1, measureOffset.y)
                    )
                } else
                    drawRect(
                        measureColor,
                        Offset(x = lineStartX, y = 0f),
                        size = Size(dpmm * 8 * -1, measureOffset.y)
                    )
            }

        }

        for (i in 0 until scalaCalculator.getHeightInMM(size)) {

            val newLineStart = scalaCalculator.getScalaLineOffsetStart(i, size)

            val newLineEnd = scalaCalculator.getScalaLineOffsetEnd(lineCounter = i, drawSize = size)

            var lineColor = measureOnColor
            if (scalaCalculator.scalaDirection == ScalaDirection.Bottom) {
                lineColor = scalaColor
                if (newLineStart.y >= size.height - measureOffset.y) {
                    lineColor = measureOnColor
                }
            } else if (scalaCalculator.scalaDirection == ScalaDirection.Center) {

                if (newLineStart.y < startMeasured || newLineStart.y >= startMeasured+measureOffset.y)
                    lineColor = scalaColor

            } else if (newLineStart.y >= measureOffset.y) {
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
                val measuredText = textMeasurer.measure(
                    AnnotatedString((absolutePosition / 10).toString()),
                    //style = TextStyle(fontSize = 18.sp)
                )

                val heightOffset = measuredText.size.height / 2F

                val transRotateOffset = 15.dp.toPx()

                var textOffset = 0F
                if (absolutePosition == 0)
                    textOffset = 25F

                var textX = scalaCalculator.dpmm * 8
                if (scalaCalculator.scalaPosition == ScalaPosition.Right)
                    textX = size.width - textX - measuredText.size.width - transRotateOffset + (textOffset * -1)
                else
                    textX += transRotateOffset + textOffset


                val lineEndPoint = scalaCalculator.getScalaLineOffsetEnd(i, size)

                withTransform({
//                   if (false /*isLandscape*/) {
//                       if (scalaCalculator.scalaPosition == ScalaPosition.Right) {
//                           rotate(
//                               degrees = -90F,
//                               pivot = Offset(x = textX, y = lineEndPoint.y)
//                           )
//                       } else {
//                           translate(left = measuredText.size.height + transRotateOffset)
//                           rotate(
//                               degrees = 90F,
//                               pivot = Offset(
//                                   x = scalaCalculator.dpmm.toFloat() * 8,
//                                   y = lineEndPoint.y - heightOffset
//                               )
//                           )
//                       }
//                   } else {
                    translate(top = -heightOffset)

                    //}
                }) {
                    drawText(
                        measuredText,
                        topLeft = Offset(
                            x = textX,
                            y = lineEndPoint.y
                        ),
                        color = scalaColor
                    )
                    }
            }
        }
    }
}