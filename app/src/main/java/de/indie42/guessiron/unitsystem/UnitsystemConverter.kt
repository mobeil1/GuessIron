package de.indie42.guessiron.unitsystem

import de.indie42.guessiron.UnitSystem

class UnitsystemConverter{

    fun convert(distance: Float, sourceSystem: IUnitsystem, destinationSystem: IUnitsystem): Float{
        val baseValue = sourceSystem.convertToBaseUnit(distance)
        return destinationSystem.convertFromBaseUnit(baseValue)
    }

    fun getUnitsystem(unitSystem: UnitSystem): IUnitsystem {
        when (unitSystem) {
            UnitSystem.METRIC -> return UnitSystemBaseMetric()
            UnitSystem.IMPERIAL -> return UnitSystemImperial()
            UnitSystem.ANT -> return UnitSystemAnt()
            else -> return UnitSystemBaseMetric()
        }

    }

    fun getUnitsystemFromUnit(unit: String): IUnitsystem {
        when (unit) {
            "mm" -> return UnitSystemBaseMetric()
            "in" -> return UnitSystemImperial()
            "ant" -> return UnitSystemAnt()
            else -> return UnitSystemBaseMetric()
        }
    }
}