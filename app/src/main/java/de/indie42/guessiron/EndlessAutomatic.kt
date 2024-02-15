package de.indie42.guessiron

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.DEFAULT_ARGS_KEY
import kotlin.math.sqrt

internal class EndlessAutomaticState(
    val forceX: MutableState<Float>,
    val forceY: MutableState<Float>,
    val forceZ: MutableState<Float>,
    val speed: MutableState<Float>,
    val active: MutableState<Boolean>,
    private val sensorManager: SensorManager
): SensorEventListener {

    val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

    fun activatedAutomatic( activated: Boolean ){
        active.value = activated

        if (activated){
            sensorManager.registerListener(
                this,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
        else{
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let { nnEvent ->
            nnEvent.values?.let { nnValues ->
                forceX.value = nnValues[0]
                forceY.value = nnValues[1]
                forceZ.value = nnValues[2]

                var output = if(nnValues.size>1){
                    sqrt((nnValues.fold(0.0f) { acc, fl -> acc + fl * fl }).toDouble()).toFloat()
                }else if(nnValues.size==1){
                    nnValues.first()
                }else {
                    0.0f
                }

                speed.value = Math.round(output * 100.0f).toFloat() / 100.00f
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}

@Composable
internal fun rememberSensorState(
    autoStart: Boolean = true,
    onMotionEvent: (Long) -> Unit = {}
): EndlessAutomaticState {

    val sensorManager = LocalContext.current.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    val sensorActive = remember { mutableStateOf(autoStart) }
    val forceX = remember { mutableStateOf(0F) }
    val forceY = remember { mutableStateOf(0F) }
    val forceZ = remember { mutableStateOf(0F) }
    val speed = remember { mutableStateOf(0F) }
    val sensorState = remember { mutableStateOf(EndlessAutomaticState(forceX = forceX, forceY = forceY, forceZ = forceZ, speed = speed, active = sensorActive, sensorManager = sensorManager)) }

    LaunchedEffect(key1 = sensorState, block = {
        sensorState.value.activatedAutomatic(autoStart)
    })

    DisposableEffect(
        key1 = sensorState,
        key2 = autoStart,
        key3 = null,
        effect = {

            onDispose {
                sensorState.value.activatedAutomatic(false)
            }
        }
    )

    return sensorState.value
}
