package de.indie42.guessiron.unitsystem

import de.indie42.guessiron.UnitSystem

interface IUnitsystem {
    fun getUnitsystem(): UnitSystem
    fun getUnit(): String
    fun getFormat(): String
    fun getDecimal(): Int
    fun convertToBaseUnit(distance: Float): Float
    fun convertFromBaseUnit(distance: Float): Float

    fun applDimension(scaleFactor: Float):Float
}