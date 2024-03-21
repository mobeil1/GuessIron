package de.indie42.guessiron.scalecalculator.position

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import de.indie42.guessiron.scalecalculator.orientation.IScaleOrientation

class ScaleOnRightPosition(
    val orientation: IScaleOrientation
): IScalePosition {
    override fun getScaleStartX(drawSize: Size): Float {
        return orientation.getWidth(drawSize)
    }
    override fun getScaleEndOffset(drawSize: Size, offset: Offset): Offset {
        return Offset(x = getScaleStartX(drawSize) - offset.x, y = offset.y)
    }
}