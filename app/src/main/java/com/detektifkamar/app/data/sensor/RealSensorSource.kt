package com.detektifkamar.app.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Sumber data sensor perangkat fisik asli Android.
 * Mengimplementasikan praktik terbaik anti-bug sensor Android:
 * - values.copyOf() wajib karena array SensorEvent.values dipakai ulang oleh OS.
 * - conflate() dan unregister saat listener dilepas di awaitClose.
 */
class RealSensorSource(context: Context) : SensorSource {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val magneticSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    private val accelerometerSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val lightSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    override val isLightSensorAvailable: Boolean
        get() = lightSensor != null

    override fun getMagneticFieldFlow(): Flow<RawMagneticData> = callbackFlow {
        if (magneticSensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.sensor.type != Sensor.TYPE_MAGNETIC_FIELD) return
                // PENTING: copyOf wajib dipanggil karena event.values dimutasi oleh sistem
                val values = event.values.copyOf()
                val x = values[0]
                val y = values[1]
                val z = values[2]
                val magnitude = sqrt(x * x + y * y + z * z)

                trySend(
                    RawMagneticData(
                        x = x,
                        y = y,
                        z = z,
                        magnitude = magnitude,
                        accuracy = event.accuracy,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Akurasi diperbarui langsung di event SensorEvent
            }
        }

        sensorManager.registerListener(listener, magneticSensor, SensorManager.SENSOR_DELAY_GAME)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.conflate()

    override fun getAccelerometerFlow(): Flow<RawAccelerometerData> = callbackFlow {
        if (accelerometerSensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
                // PENTING: copyOf wajib dipanggil
                val values = event.values.copyOf()
                val x = values[0]
                val y = values[1]
                val z = values[2]
                val magnitude = sqrt(x * x + y * y + z * z)

                // Hitung tilt dari rasio sumbu Z terhadap magnitudo gravitasi
                val cosTilt = if (magnitude > 0.001f) {
                    min(1.0f, max(-1.0f, z / magnitude))
                } else 1.0f
                val tiltDeg = Math.toDegrees(acos(cosTilt.toDouble())).toFloat()

                trySend(
                    RawAccelerometerData(
                        x = x,
                        y = y,
                        z = z,
                        magnitude = magnitude,
                        tiltDeg = tiltDeg,
                        timestamp = System.currentTimeMillis()
                    )
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.conflate()

    override fun getLightFlow(): Flow<Float?> = callbackFlow {
        if (lightSensor == null) {
            trySend(null)
            awaitClose {}
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null || event.sensor.type != Sensor.TYPE_LIGHT) return
                val values = event.values.copyOf()
                trySend(values[0])
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }.conflate()
}
