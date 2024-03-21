package de.indie42.guessiron.unitsystem

import android.content.res.Resources
import android.util.TypedValue
import de.indie42.guessiron.UnitSystem

class UnitSystemAnt: IUnitsystem {
    override fun getUnitsystem(): UnitSystem {
        return UnitSystem.ANT
    }

    override fun getUnit(): String {
        return "ant"
    }

    override fun getFormat(): String {
        return "%.1f"
    }

    override fun getDecimal(): Int {
        return 10
    }

    private val factor = 12.0F

    override fun convertToBaseUnit(distance: Float): Float {
        return distance * factor
    }

    override fun convertFromBaseUnit(distance: Float): Float {
        return distance / factor
    }

    override fun applDimension(scaleFactor: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_IN,
            scaleFactor,
            Resources.getSystem().displayMetrics
        ) * (1.0F / (25.4F/factor) )
    }
}