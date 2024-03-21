package de.indie42.guessiron

import android.content.res.Configuration
import android.content.res.Resources
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
import de.indie42.guessiron.unitsystem.IUnitsystem
import de.indie42.guessiron.unitsystem.UnitSystemBaseMetric
import de.indie42.guessiron.unitsystem.UnitsystemConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GuessIronUiState(
    val disclaimerDisabled: Boolean = true,
    val endlessMeasureHowToDisabled: Boolean = true,
    val scalaDirection: ScalaDirection,
    val scalaFactor: Float = 1F,
    val scalaOffset: Float = 0F,
    val scalaOffsetActive: Boolean = false,
    val scalaOffsetTop: Float = 0F,
    val scalaOffsetBottom: Float = 0F,
    val endlessModeActive: Boolean = false,
    val measuredPixel: Float = 0F,
    val measuredDistance: Float = 0F,
    val endlessValue: Float = 0F,
    val unitSystem: IUnitsystem = UnitSystemBaseMetric(),
    val scalaOffsetAnimation: AnimationSpec<Float> = snap(),
    val sizeInPixel: Int = 0,
    val sizeInDistance: Float = 0F,
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

    private val unitsystemConverter = UnitsystemConverter()

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
                else -> 0F
            }

            val newUnitsystem = if (currentState.unitSystem.getUnitsystem() != item.unitSystem){
                unitsystemConverter.getUnitsystem(item.unitSystem) } else
                currentState.unitSystem

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
                endlessValue = if (item.endlessModeActive) unitsystemConverter.convert(currentState.endlessValue, currentState.unitSystem, newUnitsystem) else 0F,
                measuredPixel = convertDistanceToPixel(measuredDistanceWithOffset),
                measuredDistance = unitsystemConverter.convert(currentState.measuredDistance, currentState.unitSystem, newUnitsystem),
                unitSystem = newUnitsystem,
                scalaOffsetAnimation = snap(),
                sizeInDistance = convertPixelToDistance(currentState.sizeInPixel.toFloat(), offset.toFloat()),
                endlessAutomaticSensitivity = if ( item.automacticSetting.sensitivity > 0 ) item.automacticSetting.sensitivity else currentState.endlessAutomaticSensitivity,
                endlessAutomaticSettlingTime = if ( item.automacticSetting.settlingTime >= 500 ) item.automacticSetting.settlingTime else currentState.endlessAutomaticSettlingTime
            )
        }
    }

    private fun getCurrentScalaOffset(scalaOffsetActive: Boolean, scalaOffset: Float): Float {
        if (scalaOffsetActive)
            return scalaOffset

        return 0F
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

    suspend fun changeUnitsystem(unitSystem: UnitSystem) {

        val destinationUnitsystem = unitsystemConverter.getUnitsystem(unitSystem)

        val scaleOffsetTop = unitsystemConverter.convert(_uiState.value.scalaOffsetTop, _uiState.value.unitSystem, destinationUnitsystem)
        val scaleOffsetBottom = unitsystemConverter.convert(_uiState.value.scalaOffsetBottom, _uiState.value.unitSystem, destinationUnitsystem)

        val newDisplayBorder = DisplayBorder.newBuilder()
            .setTop(scaleOffsetTop)
            .setBottom(scaleOffsetBottom)
            .build()

        guessIronDataRepository.changeUnitsystem(unitSystem, newDisplayBorder)
    }

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

            setMeasuredValue(0F, "")
        }

    }

    suspend fun changeAutomaticSensitivity(sensitivity: Float) {
        guessIronDataRepository.changeSensitivity(sensitivity)
    }

    suspend fun changeAutomaticSettlingTime(settlingTime: Int) {
        guessIronDataRepository.changeSettlingTime(settlingTime)
    }

    suspend fun changeDisplayBorder(displayBorderTop: Float, displayBorderBottom: Float) {

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
                measuredDistance = convertPixelToDistance(y, getCurrentScalaOffset(
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
                measuredDistance = convertPixelToDistance(
                    measuredPixelOnScala,
                    currentState.scalaOffset
                )
            )
        }
    }

    fun setMeasuredValue(distance: Float, unit: String) {

        val unitWithDefault = if (unit == "") "mm" else unit



        val distanceInCurrentUnit = if ( _uiState.value.unitSystem.getUnit() != unitWithDefault){
            val sourceUnitsystem = unitsystemConverter.getUnitsystemFromUnit(unitWithDefault)

            unitsystemConverter.convert(distance, sourceUnitsystem, _uiState.value.unitSystem )
        } else
            distance

        _uiState.update { currentState ->

            val measuredPixel = convertDistanceToPixel(distanceInCurrentUnit - getCurrentScalaOffset(
                currentState.scalaOffsetActive,
                currentState.scalaOffset
            ) )

            currentState.copy(
                measuredPixel = measuredPixel,
                measuredDistance = distanceInCurrentUnit,
                endlessValue = 0F
            )
        }
    }

    fun setSizeInPixel(sizeinPixel: Int) {

        _uiState.update { currentState ->

            val offset = if (currentState.scalaOffsetActive) currentState.scalaOffsetTop + currentState.scalaOffsetBottom else 0F

            currentState.copy(
                sizeInPixel = sizeinPixel,
                sizeInDistance = convertPixelToDistance(sizeinPixel.toFloat(), offset)
            )
        }
    }

    fun autoIncEndlessStepValue(){
        viewModelScope.launch {
            _uiState.update { currentState ->

                currentState.copy(
                    endlessValue = currentState.sizeInDistance + currentState.endlessValue,
                    scalaOffsetAnimation = tween(durationMillis = 750, easing = LinearOutSlowInEasing)
                )
            }
        }
    }

    fun setMeasuredEndlessStepValue(millimeter: Float) {

        _uiState.update { currentState ->
            currentState.copy(
                endlessValue = if (millimeter < 0 ) 0F else millimeter,
                scalaOffsetAnimation = tween(durationMillis = 750, easing = LinearOutSlowInEasing)
            )
        }
    }

    fun convertDistanceToPixel(distance: Float): Float {

        val dpunitWithScalaFactor = getdpUnit()

        return dpunitWithScalaFactor * distance
    }

    fun convertPixelToDistance(measuredPixel: Float, offsetDistance: Float): Float {

        val dpunitWithScalaFactor = getdpUnit()

        val measuredDistance = (measuredPixel / dpunitWithScalaFactor) + offsetDistance

        if (measuredDistance < 0)
            return measuredDistance * -1

        return measuredDistance
    }

    private fun getdpUnit(): Float {
        return _uiState.value.unitSystem.applDimension(currentScalaFactor)
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