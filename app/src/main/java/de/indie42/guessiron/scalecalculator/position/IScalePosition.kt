package de.indie42.guessiron.scalecalculator.position

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

interface IScalePosition {
    fun getScaleStartX(drawSize: Size): Float
    fun getScaleEndOffset(drawSize: Size, offset: Offset): Offset
}