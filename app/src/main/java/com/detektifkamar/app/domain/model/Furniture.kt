package com.detektifkamar.app.domain.model

data class Furniture(
    val id: Long = 0L,
    val caseId: Long,
    val label: String,
    val kind: String,
    val xM: Float,
    val yM: Float,
    val wM: Float,
    val hM: Float
)
