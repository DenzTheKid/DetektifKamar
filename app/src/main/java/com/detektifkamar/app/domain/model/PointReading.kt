package com.detektifkamar.app.domain.model

data class PointReading(
    val id: Long = 0L,
    val pointId: Long,
    val meanUt: Float,
    val minUt: Float,
    val maxUt: Float,
    val stdUt: Float,
    val bx: Float,
    val by: Float,
    val bz: Float,
    val luxMean: Float? = null,
    val azimuthDeg: Float? = null,
    val tiltDeg: Float = 0f,
    val accuracy: Int = 3,
    val quality: String = ReadingQuality.BAIK.value,
    val sampleCount: Int = 0,
    val measuredAt: Long = System.currentTimeMillis()
)
