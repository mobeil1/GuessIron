package de.indie42.guessiron.scalecalculator.orientation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

interface IScaleOrientation {
    fun getHeight(drawSize: Size): Float
    fun getWidth(drawSize: Size): Float
    fun getOffset(offset: Offset): Offset
}