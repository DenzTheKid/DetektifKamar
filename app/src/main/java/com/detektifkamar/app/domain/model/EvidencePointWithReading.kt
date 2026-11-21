package com.detektifkamar.app.domain.model

data class EvidencePointWithReading(
    val point: EvidencePoint,
    val latestReading: PointReading? = null,
    val deltaUt: Float? = null,
    val category: DisturbanceCategory = DisturbanceCategory.TENANG,
    val linkedSuspects: List<Suspect> = emptyList(),
    val customSuspectLabel: String? = null
)
