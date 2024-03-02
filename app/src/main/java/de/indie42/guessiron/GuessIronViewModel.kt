package de.indie42.guessiron

import android.content.res.Configuration
import android.content.res.Resources
import android.util.TypedValue
import android.view.Surface
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.IntSize
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class GuessIronUiState(
    val disclaimerDisabled: Boolean = true,
    val endlessMeasureHowToDisabled: Boolean = true,
    val scalaDirection: ScalaDirection,
    val scalaFactor: Float = 1F,
    val scalaOffset: Int = 0,
    val scalaOffsetActive: Boolean = false,
    val scalaOffsetTop: Int = 0,
    val scalaOffsetBottom: Int = 0,
    val endlessModeActive: Boolean = false,
    val measuredPixel: Float = 0F,
    val measuredDistance: Int = 0,
    val endlessValue: Int = 0,
    val measuredUnit: String = "mm",
    val scalaOffsetAnimation: AnimationSpec<Int> = snap(),
    val sizeInDp: Int = 0,
    val sizeInMM: Int = 0,
    val endlessAutomaticSensitivity: Float = 0.5F,
    val endlessAutomaticSettlingTime: Int = 1000,
    val endlessAutomaticInfoDisabled: Boolean = false
)

data class GuessIronDataState(
    val orderByDate: GuessIronData.SortOrder = GuessIronData.SortOrder.BY_Timestamp,
    val measuredValues: List<MeasuredValue> = arrayListOf(),
    val displayBorder: DisplayBorder = DisplayBorder.getDefaultInstance(),
)
class GuessIronViewModel(
    private val guessIronDataRepository: GuessIronDataRepository,
) : ViewModel() {

    private var currentScalaFactor = 1F

    private val _uiState = MutableStateFlow(GuessIronUiState(scalaDirection = ScalaDirection.Top))
    val uiState: StateFlow<GuessIronUiState> = _uiState.asStateFlow()

    val dataState: StateFlow<GuessIronDataState> = guessIronDataRepository.guessIronDataFlow
        .map { item ->

            copyValuesToUiState(item)

            validateScalaFactor(item)

            GuessIronDataState(
                measuredValues = sortMeasuredValues(item),
                orderByDate = item.sortOrder,
                displayBorder = item.displayBorder,
            )

        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, GuessIronDataState())

    private fun copyValuesToUiState(item: GuessIronData) {
        val scalaDirection = ScalaDirection.values().first { it.value == item.scalaDirection }

        _uiState.update { currentState ->

            val scalaOffset = when (scalaDirection) {
                ScalaDirection.Top -> item.displayBorder.top
                ScalaDirection.Bottom -> item.displayBorder.bottom
                else -> 0
            }

            val measuredDistanceWithOffset = currentState.measuredDistance - getCurrentScalaOffset(item.scalaOffsetActive, scalaOffset)

            val offset = if (item.scalaOffsetActive)item.displayBorder.top + item.displayBorder.bottom else 0

            currentState.copy(
                disclaimerDisabled = item.disclaimerDisabled,
                endlessAutomaticInfoDisabled = item.automacticSetting.disableInfo,
                endlessMeasureHowToDisabled = item.howToEndlessDisabled,
                scalaFactor = currentScalaFactor,
                scalaDirection = scalaDirection,
                scalaOffset = scalaOffset,
                scalaOffsetActive = item.scalaOffsetActive,
                scalaOffsetTop = item.displayBorder.top,
                scalaOffsetBottom = item.displayBorder.bottom,
                endlessModeActive = item.endlessModeActive,
                endlessValue = if (item.endlessModeActive) currentState.endlessValue else 0,
                measuredPixel = calculatePixelFromMM(measuredDistanceWithOffset),
                scalaOffsetAnimation = snap(),
                sizeInMM = calculateMMFromPixel(currentState.sizeInDp.toFloat(), offset),
                endlessAutomaticSensitivity = if ( item.automacticSetting.sensitivity > 0 ) item.automacticSetting.sensitivity else currentState.endlessAutomaticSensitivity,
                endlessAutomaticSettlingTime = if ( item.automacticSetting.settlingTime >= 500 ) item.automacticSetting.settlingTime else currentState.endlessAutomaticSettlingTime
            )
        }
    }

    private fun getCurrentScalaOffset(scalaOffsetActive: Boolean, scalaOffset: Int): Int {
        if (scalaOffsetActive)
            return scalaOffset

        return 0
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

    //private val displayMetrics = Resources.getSystem().displayMetrics

    suspend fun toggleScalaOffset(toggleScalaOffset: Boolean) {
        guessIronDataRepository.changeScalaOffsetActive(!toggleScalaOffset)
    }

    suspend fun toggleEndlessMode(toggleEndlessMode: Boolean) {
        guessIronDataRepository.changeEndlessModeActive(!toggleEndlessMode)
    }

    suspend fun switchScala(scalaDirection: ScalaDirection) {

        val newScalaDirection = when (scalaDirection) {
            ScalaDirection.Top -> ScalaDirection.Bottom
            ScalaDirection.Bottom -> ScalaDirection.Center
            ScalaDirection.Center -> ScalaDirection.Top
        }

        guessIronDataRepository.changeScalaDirection(newScalaDirection)
    }

    suspend fun changeScalaFactor(scalaFactor: Float) {

        if (scalaFactor in 0.5F..3F) {
            currentScalaFactor = scalaFactor
            guessIronDataRepository.changeScalaFactor(scalaFactor)

            setMeasuredValue(0)
        }

    }

    suspend fun changeAutomaticSensitivity(sensitivity: Float) {
        guessIronDataRepository.changeSensitivity(sensitivity)
    }

    suspend fun changeAutomaticSettlingTime(settlingTime: Int) {
        guessIronDataRepository.changeSettlingTime(settlingTime)
    }

    suspend fun changeDisplayBorder(displayBorderTop: Int, displayBorderBottom: Int) {

        val newDisplayBorder =
            DisplayBorder.newBuilder()
                .setTop(displayBorderTop)
                .setBottom(displayBorderBottom).build()

        guessIronDataRepository.changeDisplayBorder(newDisplayBorder)
    }

    suspend fun disableDisclaimer() {
        guessIronDataRepository.disableDisclaimer()
    }

    suspend fun disableEndlessAutomaticInfo() {
        guessIronDataRepository.disableEndlessAutomaticInfo()
    }

    suspend fun disableEndlessMeasureHowTo() {
        guessIronDataRepository.disableEndlessMeasureHowTo()
    }

    suspend fun changeSortBy(sort: GuessIronData.SortOrder) {
        guessIronDataRepository.changeSortBy(sort)
    }

    suspend fun addMeasuredValue(measuredValue: MeasuredValue) {
        guessIronDataRepository.addMeasuredValue(measuredValue)
    }

    suspend fun updateMeasuredValue(measuredValue: MeasuredValue, name: String) {
        guessIronDataRepository.updateMeasuredValue(measuredValue, name)
    }

    suspend fun removeMeasuredValue(measuredValue: MeasuredValue) {
        guessIronDataRepository.removeMeasuredValue(measuredValue)
    }

    fun increaseMeasuredPixel(
        measuredPixelOffset: Float,
        startY: Float,
        surfaceSize: IntSize,
        displayRotation: Int
    ) {

        val isLandscape =
            Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val displayCenter =
            if (isLandscape)
                surfaceSize.width / 2
            else
                surfaceSize.height / 2

        _uiState.update { currentState ->

            var y = when (scalaDirectionByRotation(currentState, displayRotation)) {
                ScalaDirection.Top -> currentState.measuredPixel + measuredPixelOffset
                ScalaDirection.Bottom -> currentState.measuredPixel - measuredPixelOffset
                ScalaDirection.Center -> if (startY < displayCenter)
                    currentState.measuredPixel - measuredPixelOffset * 2
                else
                    currentState.measuredPixel + measuredPixelOffset * 2
            }

            if (y < 0)
                y = 0f

            var maxPixel = surfaceSize.height
            if (isLandscape)
                maxPixel = surfaceSize.width

            if (y > maxPixel)
                y = maxPixel.toFloat()

            currentState.copy(
                measuredPixel = y,
                measuredDistance = calculateMMFromPixel(y, getCurrentScalaOffset(
                    currentState.scalaOffsetActive,
                    currentState.scalaOffset
                ))
            )

        }
    }

    private fun scalaDirectionByRotation(
        currentState: GuessIronUiState,
        displayRotation: Int
    ): ScalaDirection {
        var scalaOrientation = currentState.scalaDirection
        if (displayRotation == Surface.ROTATION_180 || displayRotation == Surface.ROTATION_270) {
            scalaOrientation = when (scalaOrientation) {
                ScalaDirection.Top -> ScalaDirection.Bottom
                ScalaDirection.Bottom -> ScalaDirection.Top
                else -> scalaOrientation
            }
        }
        return scalaOrientation
    }

    fun updateMeasuredPixel(measuredPixel: Float, surfaceSize: IntSize, displayRotation: Int) {

        val isLandscape =
            Resources.getSystem().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val displayCenter =
            if (isLandscape)
                surfaceSize.width / 2
            else
                surfaceSize.height / 2

        val displayPixel = if (isLandscape) surfaceSize.width else surfaceSize.height

        _uiState.update { currentState ->

            var measuredPixelOnScala = when (scalaDirectionByRotation(currentState, displayRotation)) {
                ScalaDirection.Top -> measuredPixel
                ScalaDirection.Bottom -> displayPixel - measuredPixel
                ScalaDirection.Center -> (displayCenter - measuredPixel) * 2
            }

            if (measuredPixelOnScala < 0)
                measuredPixelOnScala *= -1

            currentState.copy(
                measuredPixel = measuredPixelOnScala,
                measuredDistance = calculateMMFromPixel(
                    measuredPixelOnScala,
                    currentState.scalaOffset
                )
            )
        }
    }

    fun setMeasuredValue(millimeter: Int) {

        _uiState.update { currentState ->

            val measuredPixel = calculatePixelFromMM(millimeter - getCurrentScalaOffset(
                currentState.scalaOffsetActive,
                currentState.scalaOffset
            ) )

            currentState.copy(
                measuredPixel = measuredPixel,
                measuredDistance = millimeter,
                endlessValue = 0
            )
        }
    }

    fun setSizeInDp(sizeinDp: Int) {

        _uiState.update { currentState ->

            val offset = if (currentState.scalaOffsetActive) currentState.scalaOffsetTop + currentState.scalaOffsetBottom else 0

            currentState.copy(
                sizeInDp = sizeinDp,
                sizeInMM = calculateMMFromPixel(sizeinDp.toFloat(), offset)
            )
        }
    }

    fun autoIncEndlessStepValue(){
        viewModelScope.launch {
            _uiState.update { currentState ->

                currentState.copy(
                    endlessValue = currentState.sizeInMM + currentState.endlessValue,
                    scalaOffsetAnimation = tween(durationMillis = 750, easing = LinearOutSlowInEasing)
                )
            }
        }
    }

    fun setMeasuredEndlessStepValue(millimeter: Int) {

        _uiState.update { currentState ->

            currentState.copy(
                endlessValue = if (millimeter < 0 ) 0 else millimeter,
                scalaOffsetAnimation = tween(durationMillis = 750, easing = LinearOutSlowInEasing)
            )
        }
    }

    fun calculatePixelFromMM(millimeter: Int): Float {

        val dpmm = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_MM,
            currentScalaFactor,
            Resources.getSystem().displayMetrics
        )

        return dpmm * millimeter.toFloat()
    }

    fun calculateMMFromPixel(measuredPixel: Float, offsetMM: Int): Int {

        val dpmm = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_MM,
            currentScalaFactor,
            Resources.getSystem().displayMetrics
        )

        val measuredMM = (measuredPixel / dpmm).roundToInt() + offsetMM

        if (measuredMM < 0)
            return measuredMM * -1

        return measuredMM
    }
}

class GuessIronViewModelFactory(
    private val userPreferencesRepository: GuessIronDataRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GuessIronViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GuessIronViewModel(userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}