package de.indie42.guessiron.scalecalculator.direction

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

interface IScaleDirection {
    fun getLineLengthAtZero(): Float
    fun getStrokeWidthAtZero(): Float
    fun getOffset(drawSize: Size, dpUnit: Float): Offset
    fun getLineCounterInPixel(position: Float, offset: Float, drawSize: Size): Float
    fun getLineCounter(lineCounter: Int, startDistance: Float, getHeight: () -> Float): Float
}