package de.indie42.guessiron

import android.content.res.Resources
import android.util.TypedValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.indie42.guessiron.data.GuessIronDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.math.roundToInt

data class GuessIronUiState(
    val scalaDirection: ScalaDirection,
    val measuredPixel: Float = 0F,
    val measuredDistance: Int = 0,
    val measuredUnit: String = "mm",
)
data class GuessIronDataState(
    val scalaFactor: Float = 1F,
    val disclaimerDisabled: Boolean = true,
    val orderByDate: GuessIronData.SortOrder = GuessIronData.SortOrder.BY_Timestamp,
    val measuredValues: List<MeasuredValue> = arrayListOf()
)

class GuessIronViewModel(
    private val guessIronDataRepository: GuessIronDataRepository
) : ViewModel() {

    private var currentScalaFactor = 1F

    private val _uiState = MutableStateFlow(GuessIronUiState(scalaDirection = ScalaDirection.Top) )
    val uiState: StateFlow<GuessIronUiState> = _uiState.asStateFlow()

    val dataState: StateFlow<GuessIronDataState> = guessIronDataRepository.guessIronDataFlow
        .map { item ->

            copyScalaDirectionToUiState(item)

            validateScalaFactor(item)

            GuessIronDataState(
                scalaFactor = currentScalaFactor,
                measuredValues = sortMeasuredValues(item),
                orderByDate = item.sortOrder,
                disclaimerDisabled = item.disclaimerDisabled)

        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, GuessIronDataState())

    private fun copyScalaDirectionToUiState(item: GuessIronData) {
        val scalaDirection = ScalaDirection.values().first { it.value == item.scalaDirection }

        _uiState.update { currentState ->
            currentState.copy(
                scalaDirection = scalaDirection,
            )
        }
    }

    private fun validateScalaFactor(item: GuessIronData) {

        currentScalaFactor = item.scalaFactor
        if (currentScalaFactor <= 0.0F)
            currentScalaFactor = 1F
    }

    private fun sortMeasuredValues(item: GuessIronData): List<MeasuredValue> {
        return when (item.sortOrder) {
            GuessIronData.SortOrder.BY_Timestamp -> item.measuredValuesList.sortedByDescending { it.timestamp }
            GuessIronData.SortOrder.BY_Name -> item.measuredValuesList.sortedBy { it.name }
            else -> item.measuredValuesList.sortedByDescending { it.timestamp }
        }
    }

    private val displayMetrics = Resources.getSystem().displayMetrics
    private val displayCenter = displayMetrics.heightPixels / 2

    suspend fun switchScala() {

        var newScalaDirection = ScalaDirection.Top

        _uiState.update { currentState ->

            newScalaDirection = when ( currentState.scalaDirection){
                ScalaDirection.Top -> ScalaDirection.Bottom
                ScalaDirection.Bottom -> ScalaDirection.Center
                ScalaDirection.Center -> ScalaDirection.Top
            }

            currentState.copy(
                scalaDirection = newScalaDirection,
                measuredPixel = calculatePixelFromMM(currentState.measuredDistance)
            )
        }

        guessIronDataRepository.changeScalaDirection(newScalaDirection)
    }

    suspend fun changeScalaFactor(scalaFactor: Float){

        if (scalaFactor in 0.5F..3F){
            currentScalaFactor = scalaFactor
            guessIronDataRepository.changeScalaFactor(scalaFactor)

            setMeasuredValue(0)
        }

    }

    suspend fun disableDisclaimer(){
        guessIronDataRepository.disableDisclaimer()
    }

    suspend fun changeSortBy(sort: GuessIronData.SortOrder){
        guessIronDataRepository.changeSortBy(sort)
    }

    suspend fun addMeasuredValue(measuredValue: MeasuredValue){
        guessIronDataRepository.addMeasuredValue(measuredValue)
    }

    suspend fun updateMeasuredValue(measuredValue: MeasuredValue, name: String){
        guessIronDataRepository.updateMeasuredValue(measuredValue, name)
    }

    suspend fun removeMeasuredValue(measuredValue: MeasuredValue){
        guessIronDataRepository.removeMeasuredValue(measuredValue)
    }

    fun increaseMeasuredPixel(measuredPixelOffset: Float, startY: Float) {

        _uiState.update { currentState ->

            var y = when(currentState.scalaDirection){
                ScalaDirection.Top -> currentState.measuredPixel + measuredPixelOffset
                ScalaDirection.Bottom -> currentState.measuredPixel - measuredPixelOffset
                ScalaDirection.Center -> if ( startY < displayCenter)
                    currentState.measuredPixel - measuredPixelOffset * 2
                else
                    currentState.measuredPixel + measuredPixelOffset * 2
            }

            if (y < 0)
                y = 0f

            if (y > displayMetrics.heightPixels)
                y = displayMetrics.heightPixels.toFloat()

            currentState.copy(
                measuredPixel = y,
                measuredDistance = calculateMMFromPixel(y)
            )

        }
    }

    fun updateMeasuredPixel(measuredPixel: Float) {

        _uiState.update { currentState ->

            var measuredPixelOnScala = when (currentState.scalaDirection){
                ScalaDirection.Top -> measuredPixel
                ScalaDirection.Bottom -> displayMetrics.heightPixels - measuredPixel
                ScalaDirection.Center -> (displayCenter - measuredPixel) * 2
            }

            if (measuredPixelOnScala < 0)
                measuredPixelOnScala *= -1

            currentState.copy(
                measuredPixel = measuredPixelOnScala,
                measuredDistance = calculateMMFromPixel(measuredPixelOnScala)
            )
        }
    }

    fun setMeasuredValue(millimeter: Int){

        _uiState.update { currentState ->

            val measuredPixel = calculatePixelFromMM(millimeter)

            currentState.copy(
                measuredPixel = measuredPixel,
                measuredDistance = millimeter
            )
        }
    }

    private fun calculatePixelFromMM(millimeter: Int): Float {

        val dpmm = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, currentScalaFactor, displayMetrics)

        return dpmm * millimeter.toFloat()
    }

    private fun calculateMMFromPixel(measuredPixel: Float): Int {

        val dpmm = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, currentScalaFactor, displayMetrics)

        val measuredMM = (measuredPixel / dpmm).roundToInt()

        if (measuredMM < 0)
            return measuredMM * -1

        return measuredMM
    }
}

class TasksViewModelFactory(
    private val userPreferencesRepository: GuessIronDataRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GuessIronViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GuessIronViewModel( userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}