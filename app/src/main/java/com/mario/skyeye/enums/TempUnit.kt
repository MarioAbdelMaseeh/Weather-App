package com.mario.skyeye.enums

import java.util.Locale

enum class TempUnit(val unitType: String, val enSymbol: String, val arSymbol: String) {
    CELSIUS("metric", "°C", "°م"),
    FAHRENHEIT("imperial", "°F", "°ف"),
    KELVIN("standard", "°K", "ك");

    fun getSymbol(language: String = Locale.getDefault().language): String {
        return if (language == Languages.ARABIC.code) arSymbol else enSymbol
    }
    companion object{
        fun fromUnitType(unitType: String): TempUnit? = TempUnit.entries.find { it.unitType == unitType }
        fun fromSymbol(symbol: String): TempUnit? = TempUnit.entries.find { it.enSymbol == symbol || it.arSymbol == symbol }
    }
}