package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.DisturbanceCategory
import org.junit.Assert.assertEquals
import org.junit.Test

class DisturbanceClassifierTest {

    @Test
    fun testExactBoundaryClassification() {
        // Tenang: < 5.0
        assertEquals(DisturbanceCategory.TENANG, DisturbanceClassifier.classify(0.0f))
        assertEquals(DisturbanceCategory.TENANG, DisturbanceClassifier.classify(4.99f))

        // Gangguan Ringan: 5.0 <= delta < 15.0
        assertEquals(DisturbanceCategory.GANGGUAN_RINGAN, DisturbanceClassifier.classify(5.0f))
        assertEquals(DisturbanceCategory.GANGGUAN_RINGAN, DisturbanceClassifier.classify(14.99f))

        // Gangguan Kuat: 15.0 <= delta < 50.0
        assertEquals(DisturbanceCategory.GANGGUAN_KUAT, DisturbanceClassifier.classify(15.0f))
        assertEquals(DisturbanceCategory.GANGGUAN_KUAT, DisturbanceClassifier.classify(49.99f))

        // Sumber Dekat: >= 50.0
        assertEquals(DisturbanceCategory.SUMBER_DEKAT, DisturbanceClassifier.classify(50.0f))
        assertEquals(DisturbanceCategory.SUMBER_DEKAT, DisturbanceClassifier.classify(120.0f))
    }

    @Test
    fun testDeltaCalculation() {
        val delta = DisturbanceClassifier.calculateDelta(55.5f, 45.0f)
        assertEquals(10.5f, delta, 0.001f)
    }
}
