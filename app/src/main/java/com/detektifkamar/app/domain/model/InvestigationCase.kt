package com.detektifkamar.app.domain.model

data class InvestigationCase(
    val id: Long = 0L,
    val title: String,
    val roomType: String,
    val widthM: Float,
    val lengthM: Float,
    val note: String? = null,
    val baselineUt: Float? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
