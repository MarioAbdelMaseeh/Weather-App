package com.mario.skyeye.enums

import java.util.Locale

enum class MapHelper(val mapType: String, val enDisplayName: String, val arDisplayName: String ) {
    GPS("GPS", "GPS", "نظام تحديد المواقع"),
    MAP("MAP", "Map", "خريطة");

    fun getDisplayName(language: String = Locale.getDefault().language): String {
        return if (language == "ar") arDisplayName else enDisplayName
    }
    companion object {
        fun fromMapType(mapType: String): MapHelper? {
            return MapHelper.entries.find { it.mapType == mapType }
        }
        fun fromMapDisplayName(displayName: String): MapHelper? {
            return MapHelper.entries.find { it.enDisplayName == displayName || it.arDisplayName == displayName }
        }
    }
}