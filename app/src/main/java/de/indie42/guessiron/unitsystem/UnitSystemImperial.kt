package de.indie42.guessiron.unitsystem

import android.content.res.Resources
import android.util.TypedValue
import de.indie42.guessiron.UnitSystem

class UnitSystemImperial: IUnitsystem {
    override fun getUnitsystem(): UnitSystem {
        return UnitSystem.IMPERIAL
    }

    override fun getUnit(): String {
        return "in"
    }

    override fun getFormat(): String {
        return "%.1f"
    }

    override fun getDecimal(): Int {
        return 10
    }

    private val factor = 25.4F
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
        )
    }
}