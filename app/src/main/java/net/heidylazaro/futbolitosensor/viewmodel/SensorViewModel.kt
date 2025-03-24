package net.heidylazaro.futbolitosensor.viewmodel

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AccelerometerData(val x: Float, val y: Float, val z: Float)

class SensorViewModel(application: Application) : AndroidViewModel(application) {

    var accelerometerData by mutableStateOf(AccelerometerData(0f, 0f, 0f))
        private set

    private val sensorManager = application.getSystemService(SensorManager::class.java)
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    init {
        viewModelScope.launch {
            observeAccelerometer().collectLatest {
                accelerometerData = it
            }
        }
    }

    private fun observeAccelerometer() = callbackFlow {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                trySend(
                    AccelerometerData(
                        x = event.values[0],
                        y = event.values[1],
                        z = event.values[2]
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
