package com.mario.skyeye.utils

import com.mario.skyeye.enums.Languages
import java.util.Locale

object LanguageManager {
    fun convertToArabicNumbers(number: String): String {
        val arabicDigits = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return number.map { if (it.isDigit()) arabicDigits[it.digitToInt()] else it }.joinToString("")
    }

    fun formatNumberBasedOnLanguage(number: String): String {
        val language = Locale.getDefault().language
        return if (language == Languages.ARABIC.code) convertToArabicNumbers(number) else number
    }
}