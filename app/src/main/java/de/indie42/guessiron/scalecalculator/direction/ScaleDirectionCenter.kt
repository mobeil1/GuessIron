package de.indie42.guessiron.scalecalculator.direction

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import de.indie42.guessiron.scalecalculator.orientation.IScaleOrientation
import kotlin.math.roundToInt

class ScaleDirectionCenter(
    val orientation: IScaleOrientation
): IScaleDirection {
    override fun getLineLengthAtZero(): Float {
        return 10F
    }

    override fun getStrokeWidthAtZero(): Float {
        return 7F
    }

    override fun getOffset(drawSize: Size, dpUnit: Float): Offset {

        val heightLength = orientation.getHeight(drawSize) / dpUnit

        val displayLength = heightLength.roundToInt()

        var oddOffset = Offset(x = 0F, y = 0F)

        if (displayLength % 2 != 0)
            oddOffset = Offset(x = 0F, y = 0.5F * dpUnit)

        val displayOffset = heightLength - displayLength

        val directionOffset = Offset(0F, (displayOffset / 2F) * dpUnit) + oddOffset

        return orientation.getOffset( directionOffset )
    }

    override fun getLineCounterInPixel(position: Float, offset: Float, drawSize: Size): Float {
        return position - offset
    }

    override fun getLineCounter(lineCounter: Int, startDistance: Float, getHeight: () -> Float): Float {
        // 0 ... 39 .... 78 ; pixel 6 pro -> 2891px/20.281652mm = 142,5426mm (143)
        var displayLength = getHeight()
        if ( displayLength % 2 != 0F )
            displayLength -= 1

        val center = (displayLength/2F).roundToInt()

        var counterFromCenter = center - lineCounter

        if (counterFromCenter < 0)
            counterFromCenter = lineCounter - center

        return counterFromCenter.toFloat()
    }

}