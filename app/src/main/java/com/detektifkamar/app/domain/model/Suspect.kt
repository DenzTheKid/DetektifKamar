package com.detektifkamar.app.domain.model

data class Suspect(
    val id: Long = 0L,
    val name: String,
    val category: SuspectCategory,
    val typicalMinUt: Float,
    val typicalMaxUt: Float,
    val typicalDistanceCm: Int,
    val note: String,
    val howToConfirm: String
)
