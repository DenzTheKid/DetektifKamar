package com.detektifkamar.app.domain.logic

import com.detektifkamar.app.domain.model.UnitType
import java.util.Locale

/**
 * Utilitas konversi dan formatting satuan medan magnet (µT, mT, G).
 */
object UnitConverter {

    /**
     * Mengonversi nilai dari satuan basis mikrotesla (µT) ke satuan target.
     */
    fun convert(valueUt: Float, targetUnit: UnitType): Float {
        return targetUnit.convertFromMicrotesla(valueUt)
    }

    /**
     * Mengonversi nilai dari satuan sumber kembali ke mikrotesla (µT).
     */
    fun convertToUt(value: Float, sourceUnit: UnitType): Float {
        return sourceUnit.convertToMicrotesla(value)
    }

    /**
     * Memformat nilai terkonversi menjadi teks desimal rapi beserta simbol satuannya.
     *
     * @param valueUt Nilai dasar dalam mikrotesla (µT)
     * @param targetUnit Satuan yang ingin ditampilkan
     * @param includeSymbol Apakah menyertakan simbol satuan di akhir string
     */
    fun format(
        valueUt: Float,
        targetUnit: UnitType,
        includeSymbol: Boolean = true
    ): String {
        val converted = convert(valueUt, targetUnit)
        val formattedNumber = String.format(
            Locale.US,
            "%.${targetUnit.decimalPlaces}f",
            converted
        )
        return if (includeSymbol) "$formattedNumber ${targetUnit.symbol}" else formattedNumber
    }
}
