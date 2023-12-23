package de.indie42.guessiron

import android.content.res.Resources
import android.util.TypedValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.roundToInt

class ScalaCalculator (
    val scalaDirection: ScalaDirection,
    val scalaPosition: ScalaPosition,
    scalaFactor: Float ) {

    private val displayMetrics = Resources.getSystem().displayMetrics

    val dpmm = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, 1f, displayMetrics)
    private val dpmmWithScalaFactor = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, scalaFactor, displayMetrics)

    fun getHeightInMM(drawSize: Size):Int{

        return (drawSize.height/dpmmWithScalaFactor).roundToInt()
    }

    fun getMMinPixel(mm: Int):Float{

        return mm.toFloat()*dpmmWithScalaFactor
    }

    private fun directionOffset(drawSize: Size): Offset{

       if (scalaDirection == ScalaDirection.Center){
            val heightInMM = drawSize.height / dpmmWithScalaFactor

            val displayInMM = getHeightInMM(drawSize)

           var oddOffset = Offset( x = 0F, y = 0F)
           if (displayInMM % 2 != 0)
               oddOffset = Offset (x = 0F, y = 0.5F * dpmmWithScalaFactor)

            val displayOffset = heightInMM - displayInMM

            return Offset(0F, (displayOffset / 2F) * dpmmWithScalaFactor ) + oddOffset

           // 7 ist die Liniendicke bei 0
           //return Offset ( x = 0F, y = (7F/2F)*-1F )
           //return Offset ( x = 0F, y = 0F )
        }

        return Offset(0F, 0F)
    }

    fun getLinePosition(lineCounter: Int, drawSize: Size): Int{
        if(scalaDirection == ScalaDirection.Center){
            // 0 ... 39 .... 78 ; pixel 6 pro -> 2891px/20.281652mm = 142,5426mm (143)
            var displayInMM = getHeightInMM(drawSize)
            if ( displayInMM % 2 != 0 )
                displayInMM -= 1

            val center = (displayInMM/2F).roundToInt()



            var counterFromCenter = center - lineCounter

            if (counterFromCenter < 0)
                counterFromCenter = lineCounter - center

            return counterFromCenter

        }

        return lineCounter
    }

    fun getScalaStartX(drawSize: Size): Float{

        if (scalaPosition == ScalaPosition.Right)
            return drawSize.width

        return 0f
    }

    private fun getScalaY(lineCounter: Int, drawSize: Size): Float{

        if (scalaDirection == ScalaDirection.Bottom)
            return drawSize.height - dpmmWithScalaFactor * lineCounter

        return dpmmWithScalaFactor * lineCounter
    }

    fun getScalaLineOffsetStart(lineCounter: Int, drawSize: Size): Offset {

//        var additonalOffset = Offset(0F, 0f)
//        if (scalaDirection == ScalaDirection.Center){
//            val lineAmount = getHeightInMM(drawSize) / 2;
//            if (lineCounter < lineAmount)
//                additonalOffset += Offset(0F, drawSize.height / 2)
//            else
//                additonalOffset -= Offset(0F, drawSize.height / 2)
//        }


        return Offset(x = getScalaStartX(drawSize), y = getScalaY(lineCounter, drawSize)) + directionOffset(drawSize)
    }
    fun getScalaLineOffsetEnd(lineCounter: Int, drawSize: Size): Offset {

        val lineLength = getLineLength(lineCounter, drawSize)

        if (scalaPosition == ScalaPosition.Right) {
            return Offset( x = drawSize.width - dpmm * lineLength,  y = getScalaY(lineCounter, drawSize)) + directionOffset(drawSize)
        }
        return Offset( x = dpmm * lineLength, y = getScalaY(lineCounter, drawSize)) + directionOffset(drawSize)
    }

    private fun getLineLength(lineCounter: Int, drawSize: Size): Int
    {
        val lineCounterLPosition = getLinePosition(lineCounter, drawSize)

        if (scalaDirection == ScalaDirection.Center && lineCounterLPosition == 0)
            return 10
        else if (lineCounterLPosition % 10 == 0) {
            return 8 // every 10 mm a 8 mm line
        } else if (lineCounterLPosition % 5 == 0) {
            return 5 // every 0,5 cm a 5 mm line
        }

       return 3 // 3mm Line für 1 mm
    }

    fun getStrokeWidth(lineCounter: Int, drawSize: Size): Float{

        val lineCounterLPosition = getLinePosition(lineCounter, drawSize)


        if (scalaDirection == ScalaDirection.Center && lineCounterLPosition == 0)
            return 7F
        else if (lineCounterLPosition % 10 == 0 ) {
            return 5F
        } else if (lineCounterLPosition % 5 == 0) {
            return 5F
        }

        return 3F

    }

}
