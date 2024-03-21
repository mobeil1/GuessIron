package de.indie42.guessiron.scalecalculator.orientation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

class LandscapeOrientation: IScaleOrientation {
    override fun getHeight(drawSize: Size): Float {
        return drawSize.width
    }
    override fun getWidth(drawSize: Size): Float {
        return drawSize.height
    }
    override fun getOffset(offset: Offset): Offset {
        return Offset(x = offset.y, y = offset.x)
    }
}