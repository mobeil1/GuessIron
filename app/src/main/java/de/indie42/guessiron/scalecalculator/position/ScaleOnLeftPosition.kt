package de.indie42.guessiron.scalecalculator.position

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

class ScaleOnLeftPosition: IScalePosition {
    override fun getScaleStartX(drawSize: Size): Float {
        return 0F
    }

    override fun getScaleEndOffset(drawSize: Size, offset: Offset): Offset {
        return offset
    }
}