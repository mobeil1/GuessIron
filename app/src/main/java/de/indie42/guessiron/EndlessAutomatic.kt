package de.indie42.guessiron

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlin.math.absoluteValue

internal class EndlessAutomaticState(
    val active: MutableState<Boolean>,
    val isLandscape: MutableState<Boolean>,
    val motionEvent: (Long) -> Unit,
    private val sensorManager: SensorManager
) : SensorEventListener {

    private val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)

    var sensitivity = 0.5F
    private var settlingTime = 1000

    private var lastAccX = 0F
    private var lastAccY = 0F
    private var milliMoveReconized = 0L

    fun activateAutomatic(newSensitivity: Float, newSettlingTime: Int) {
        if (!active.value) {
            sensitivity = newSensitivity
            settlingTime = newSettlingTime

            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            active.value = true
        }
    }

    fun deactivateAutomatic() {
        sensorManager.unregisterListener(this)
        active.value = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { nnEvent ->
            nnEvent.values?.let { nnValues ->

                if (nnEvent.sensor.type != Sensor.TYPE_LINEAR_ACCELERATION)
                    return

                val timeStamp = nnEvent.timestamp / 1_000_000

                // the last 2 values must be over the threshold
                val forceToHigh =
                    (nnValues[0].absoluteValue > sensitivity || nnValues[1].absoluteValue > sensitivity)

                lastAccX = nnValues[0]
                lastAccY = nnValues[1]

                if (forceToHigh) {
                    milliMoveReconized = timeStamp
                } else if (milliMoveReconized > 0) {
                    if (timeStamp - milliMoveReconized > settlingTime) {
                        motionEvent(timeStamp)
                        milliMoveReconized = 0
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
}

@Composable
internal fun rememberSensorState(
    autoStart: Boolean = false,
    sensitivity: Float = 0.5F,
    settlingTime: Int = 1000,
    isLandsacpe: Boolean = false,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    onMotionEvent: (Long) -> Unit = {}
): EndlessAutomaticState {

    val sensorManager =
        LocalContext.current.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    val sensorActive = remember { mutableStateOf(autoStart) }
    val isLandscape = remember { mutableStateOf(isLandsacpe) }
    val sensorState = remember {
        mutableStateOf(
            EndlessAutomaticState(
                active = sensorActive,
                isLandscape = isLandscape,
                motionEvent = onMotionEvent,
                sensorManager = sensorManager
            )
        )
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (autoStart)
                    sensorState.value.activateAutomatic(sensitivity, settlingTime)
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                sensorState.value.deactivateAutomatic()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return sensorState.value
}
