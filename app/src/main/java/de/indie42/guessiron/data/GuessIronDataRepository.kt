package de.indie42.guessiron.data

import android.util.Log
import androidx.datastore.core.DataStore
import de.indie42.guessiron.DisplayBorder
import de.indie42.guessiron.GuessIronData
import de.indie42.guessiron.MeasuredValue
import de.indie42.guessiron.ScalaDirection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

class GuessIronDataRepository(private val guessIronDataDataStore: DataStore<GuessIronData>) {

    private val logTag: String = "GuessIronDataRepository"

    val guessIronDataFlow: Flow<GuessIronData> = guessIronDataDataStore.data
        .catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                Log.e(logTag, "Error reading sort order preferences.", exception)
                emit(GuessIronData.getDefaultInstance())
            } else {
                throw exception
            }
        }

    suspend fun disableDisclaimer() {
        guessIronDataDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setDisclaimerDisabled(true).build()
        }
    }

    suspend fun disableEndlessMeasureHowTo() {
        guessIronDataDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setHowToEndlessDisabled(true).build()
        }
    }

    suspend fun changeDisplayBorder( displayBorder: DisplayBorder ) {
        guessIronDataDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setDisplayBorder(displayBorder).build()
        }
    }

    suspend fun changeScalaFactor(scalaFactor: Float) {
        guessIronDataDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setScalaFactor(scalaFactor).build()
        }
    }

    suspend fun changeScalaDirection(scalaDirection: ScalaDirection) {
        guessIronDataDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setScalaDirection(scalaDirection.value).build()
        }
    }

    suspend fun changeScalaOffsetActive(scalaOffsetActive: Boolean) {
        guessIronDataDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setScalaOffsetActive(scalaOffsetActive).build()
        }
    }

    suspend fun changeEndlessModeActive(endlessModeActive: Boolean) {
        guessIronDataDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setEndlessModeActive(endlessModeActive).build()
        }
    }


    suspend fun changeSortBy(sort: GuessIronData.SortOrder) {
        guessIronDataDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().setSortOrder(sort).build()
        }
    }

    suspend fun addMeasuredValue(measuredValue: MeasuredValue) {
        guessIronDataDataStore.updateData { currentPreferences ->
            currentPreferences.toBuilder().addMeasuredValues(measuredValue).build()
        }
    }

    suspend fun updateMeasuredValue(measuredValue: MeasuredValue, name: String) {
        guessIronDataDataStore.updateData { currentPreferences ->
            val indexOfValue = currentPreferences.measuredValuesList.indexOf(measuredValue)
            val measuredValueFromList = currentPreferences.measuredValuesList[indexOfValue]

            currentPreferences.toBuilder().setMeasuredValues(indexOfValue, measuredValueFromList.toBuilder().setName(name)).build()
        }
    }

    suspend fun removeMeasuredValue(measuredValue: MeasuredValue) {
        guessIronDataDataStore.updateData { currentPreferences ->
            val indexToDelete = currentPreferences.measuredValuesList.indexOf(measuredValue)
            currentPreferences.toBuilder().removeMeasuredValues(indexToDelete).build()
        }
    }
}