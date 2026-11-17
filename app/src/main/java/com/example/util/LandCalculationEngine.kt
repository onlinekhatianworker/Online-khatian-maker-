package com.example.util

import kotlin.math.floor
import kotlin.math.roundToInt

enum class LandUnit(
    val labelBn: String,
    val labelEn: String,
    val shotokPerUnit: Double,
) {
    SHOTOK("শতক", "Decimal (Shotok)", 1.0),
    ACRE("একর", "Acre", 100.0),
    KATHA("কাঠা", "Katha", 1.65),
    BIGHA("বিঘা", "Bigha", 33.0),
    HECTARE("হেক্টর", "Hectare", 247.10538),
}

object LandCalculationEngine {
    fun toShotok(amount: Double, unit: LandUnit): Double {
        if (amount < 0.0) return 0.0
        return amount * unit.shotokPerUnit
    }

    fun fromShotok(shotok: Double, targetUnit: LandUnit): Double {
        if (shotok < 0.0) return 0.0
        return shotok / targetUnit.shotokPerUnit
    }

    fun convert(amount: Double, from: LandUnit, to: LandUnit): Double {
        if (from == to) return amount
        return fromShotok(toShotok(amount, from), to)
    }

    /**
     * Splits a Shotok quantity into official Bangladesh Acre + Shotok sub-columns
     * (e.g. 125.50 shotok -> 1 Acre, 25.50 Shotok).
     */
    fun splitToAcreAndShotok(totalShotok: Double): Pair<Int, Double> {
        if (totalShotok <= 0.0) return 0 to 0.0
        val acres = floor(totalShotok / 100.0).toInt()
        val remainingShotok = ((totalShotok - acres * 100.0) * 10000.0).roundToInt() / 10000.0
        return acres to remainingShotok
    }

    /**
     * Evaluates Hissa strings cleanly without touching land area unit logic:
     * - Decimal: "0.25", "০.২৫" -> 0.25
     * - Fraction: "1/4", "১/৪" -> 0.25
     * - Percentage: "25%", "২৫%" -> 0.25
     * - Traditional Anna: "4 আনা", "৪ আনা", "8 anna" -> 0.25 (out of 16 anna)
     */
    fun parseHissaFraction(rawInput: String): Double? {
        val normalized = BengaliNumberUtils.toEnglishDigits(rawInput).trim()
        if (normalized.isEmpty()) return null

        // Check percentage
        if (normalized.endsWith("%")) {
            val pct = normalized.removeSuffix("%").trim().toDoubleOrNull() ?: return null
            return if (pct in 0.0..100.0) pct / 100.0 else null
        }

        // Check traditional Anna ("4 আনা" or "4 anna")
        val lower = normalized.lowercase()
        if (lower.contains("আনা") || lower.contains("anna")) {
            val numStr = lower
                .replace("আনা", "")
                .replace("anna", "")
                .trim()
            val anna = numStr.toDoubleOrNull() ?: return null
            return if (anna in 0.0..16.0) anna / 16.0 else null
        }

        // Check fraction "a/b"
        if (normalized.contains("/")) {
            val parts = normalized.split("/")
            if (parts.size == 2) {
                val num = parts[0].trim().toDoubleOrNull() ?: return null
                val den = parts[1].trim().toDoubleOrNull() ?: return null
                if (den > 0.0 && num >= 0.0) {
                    return num / den
                }
            }
            return null
        }

        // Plain decimal
        val dec = normalized.toDoubleOrNull() ?: return null
        return if (dec >= 0.0) dec else null
    }

    fun calculateEntitledArea(totalPlotArea: Double, hissaFraction: Double): Double {
        if (totalPlotArea < 0.0 || hissaFraction < 0.0) return 0.0
        return totalPlotArea * hissaFraction
    }
}
