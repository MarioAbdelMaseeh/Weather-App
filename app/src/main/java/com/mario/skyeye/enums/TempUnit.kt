package com.mario.skyeye.enums

import java.util.Locale

enum class TempUnit(val unitType: String, val tempEnSymbol: String, val tempArSymbol: String, val windEnSymbol: String, val windArSymbol: String) {
    METRIC("metric", "°C", "°م","m/s","م/ث"),
    IMPERIAL("imperial", "°F", "°ف","mph","ميل/س"),
    STANDARD("standard", "°K", "°ك","m/s","م/ث");

    fun getTempSymbol(language: String = Locale.getDefault().language): String {
        return if (language == Languages.ARABIC.code) tempArSymbol else tempEnSymbol
    }
    fun getWindSymbol(language: String = Locale.getDefault().language): String {
        return if (language == Languages.ARABIC.code) windArSymbol else windEnSymbol
    }
    companion object{
        fun fromUnitType(unitType: String): TempUnit? = TempUnit.entries.find { it.unitType == unitType }
        fun fromSymbol(symbol: String): TempUnit? = TempUnit.entries.find { it.tempEnSymbol == symbol || it.tempArSymbol == symbol }
        fun fromWindSymbol(symbol: String): TempUnit? = TempUnit.entries.find { it.windEnSymbol == symbol || it.windArSymbol == symbol }
        fun fromWindUnitType(unitType: String): TempUnit? = TempUnit.entries.find { it.unitType == unitType }
    }
}