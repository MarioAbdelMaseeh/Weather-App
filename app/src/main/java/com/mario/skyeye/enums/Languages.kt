package com.mario.skyeye.enums


enum class Languages(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    SPANISH("Español", "es"),
    FRENCH("Français", "fr"),
    GERMAN("Deutsch", "de"),
    ITALIAN("Italiano", "it"),
    CHINESE("中文", "zh"),
    JAPANESE("日本語", "ja"),
    ARABIC("العربية", "ar"),
    HINDI("हिन्दी", "hi"),
    PORTUGUESE("Português", "pt");
    companion object {
        fun fromCode(code: String): Languages? = Languages.entries.find { it.code == code }
        fun fromLanguageDisplayName(displayName: String): Languages? = Languages.entries.find { it.displayName == displayName }
    }
}

