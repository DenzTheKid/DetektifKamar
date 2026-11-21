package com.detektifkamar.app.domain.model

data class EvidencePoint(
    val id: Long = 0L,
    val caseId: Long,
    val label: String,
    val xM: Float,
    val yM: Float,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
