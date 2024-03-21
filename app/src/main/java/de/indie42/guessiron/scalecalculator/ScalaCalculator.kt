package de.indie42.guessiron.scalecalculator

import android.content.res.Resources
import android.util.TypedValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import de.indie42.guessiron.ScalaDirection
import de.indie42.guessiron.ScalaPosition
import de.indie42.guessiron.scalecalculator.direction.ScaleDirectionBottom
import de.indie42.guessiron.scalecalculator.direction.ScaleDirectionCenter
import de.indie42.guessiron.scalecalculator.direction.ScaleDirectionTop
import de.indie42.guessiron.scalecalculator.orientation.LandscapeOrientation
import de.indie42.guessiron.scalecalculator.orientation.PortraitOrientation
import de.indie42.guessiron.scalecalculator.position.ScaleOnLeftPosition
import de.indie42.guessiron.scalecalculator.position.ScaleOnRightPosition
import de.indie42.guessiron.unitsystem.IUnitsystem
import kotlin.math.roundToInt


class ScalaCalculator (
    val scalaDirection: ScalaDirection,
    val scalaPosition: ScalaPosition,
    scalaFactor: Float,
    startDistance: Float,
    val unitSystem: IUnitsystem,
    val isLandsacpe: Boolean) {

    private val startDistance = startDistance * unitSystem.getDecimal()

    private val startDistanceFraction = this.startDistance % 1

    private val orientation = if (isLandsacpe) LandscapeOrientation() else PortraitOrientation()

    private val scalePosition = if (scalaPosition == ScalaPosition.Right) ScaleOnRightPosition(orientation) else ScaleOnLeftPosition()

    private val scaleDirection = if ( scalaDirection == ScalaDirection.Center) ScaleDirectionCenter(orientation) else if ( scalaDirection == ScalaDirection.Bottom) ScaleDirectionBottom(orientation) else ScaleDirectionTop()

    private val displayMetrics = Resources.getSystem().displayMetrics

    private val dpmm = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, displayMetrics)

    private val dpunitWithScalaFactor = unitSystem.applDimension(scalaFactor) / unitSystem.getDecimal()

    fun getHeightLength(drawSize: Size):Int{
        return (getScaleLengthInPixel(drawSize)/dpunitWithScalaFactor).roundToInt()
    }

    fun getScaleLengthInPixel(drawSize: Size): Float{
        return orientation.getHeight(drawSize)
    }

    fun getDistanceinPixel(distance: Float):Float{
        return distance*dpunitWithScalaFactor
    }

    private fun directionOffset(drawSize: Size): Offset{
        return scaleDirection.getOffset(drawSize, dpunitWithScalaFactor)
    }

    fun getLinePosition(lineCounter: Int, drawSize: Size): Int{
        return scaleDirection.getLineCounter(lineCounter, startDistance, getHeight = {
            return@getLineCounter getHeightLength(drawSize).toFloat()
        } ).toInt()
    }

    fun getScalaStartX(drawSize: Size): Float{
        return scalePosition.getScaleStartX(drawSize)
    }

    private fun getScalaY(lineCounter: Int, drawSize: Size): Float{

        return scaleDirection.getLineCounterInPixel( dpunitWithScalaFactor * lineCounter, startDistanceFraction * dpunitWithScalaFactor, drawSize = drawSize )
    }

    fun getScalaLineOffsetStart(lineCounter: Int, drawSize: Size): Offset {
        val offset = orientation.getOffset( Offset(x = getScalaStartX(drawSize), y = getScalaY(lineCounter, drawSize)) )

        return offset + directionOffset(drawSize)
    }

    fun getScalaLineOffsetEnd(lineCounter: Int, drawSize: Size): Offset {

        val lineLength = getLineLength(lineCounter, drawSize)

        val rawOffset = Offset( x = lineLength, y = getScalaY(lineCounter, drawSize) )

        val positionOffset = scalePosition.getScaleEndOffset(drawSize, rawOffset)

        val orientationOffset = orientation.getOffset( positionOffset )

        return orientationOffset + directionOffset(drawSize)

    }

    val MAX_LINE = 8F * dpmm

    fun getLineLength(lineCounter: Int, drawSize: Size): Float
    {
        val lineCounterLPosition = getLinePosition(lineCounter, drawSize)

        if (lineCounterLPosition == 0)
            return scaleDirection.getLineLengthAtZero() * dpmm
        else if (lineCounterLPosition % 10 == 0) {
            return MAX_LINE // every 10 mm a 8 mm line
        } else if (lineCounterLPosition % 5 == 0) {
            return 5F * dpmm // every 0,5 cm a 5 mm line
        }

        return 3F * dpmm // 3mm Line für 1 mm
    }

    fun getStrokeWidth(lineCounter: Int, drawSize: Size): Float{

        val lineCounterLPosition = getLinePosition(lineCounter, drawSize)

        if (lineCounterLPosition == 0)
            return scaleDirection.getStrokeWidthAtZero()
        else if (lineCounterLPosition % 10 == 0 ) {
            return 5F
        } else if (lineCounterLPosition % 5 == 0) {
            return 5F
        }

        return 3F

    }

}