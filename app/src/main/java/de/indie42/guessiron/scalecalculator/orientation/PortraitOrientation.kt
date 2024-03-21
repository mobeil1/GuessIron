package de.indie42.guessiron.scalecalculator.orientation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

class PortraitOrientation: IScaleOrientation {
    override fun getHeight(drawSize: Size): Float {
        return drawSize.height
    }
    override fun getWidth(drawSize: Size): Float {
        return drawSize.width
    }
    override fun getOffset(offset: Offset): Offset {
        return offset
    }
}