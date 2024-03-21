package de.indie42.guessiron.scalecalculator.direction

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

class ScaleDirectionTop: IScaleDirection {
    override fun getLineLengthAtZero(): Float {
        return 0F
    }

    override fun getStrokeWidthAtZero(): Float {
        return 0.0F
    }

    override fun getOffset(drawSize: Size, dpUnit: Float): Offset {
        return Offset(x = 0F, y = 0F)
    }

    override fun getLineCounterInPixel(position: Float, offset: Float, drawSize: Size): Float {
        return position - offset
    }

    override fun getLineCounter(lineCounter: Int, startDistance: Float, getHeight: () -> Float): Float {
        return lineCounter + startDistance
    }
}