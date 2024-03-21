package de.indie42.guessiron.unitsystem

import android.content.res.Resources
import android.util.TypedValue
import de.indie42.guessiron.UnitSystem

class UnitSystemBaseMetric: IUnitsystem {
    override fun getUnitsystem(): UnitSystem {
        return UnitSystem.METRIC
    }

    override fun getUnit(): String {
        return "mm"
    }

    override fun getFormat(): String {
        return "%.0f"
    }

    override fun getDecimal(): Int {
        return 1
    }

    override fun convertToBaseUnit(distance: Float): Float {
        return distance
    }

    override fun convertFromBaseUnit(distance: Float): Float {
        return distance
    }

    override fun applDimension(scaleFactor: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_MM,
            scaleFactor,
            Resources.getSystem().displayMetrics
        )
    }
}