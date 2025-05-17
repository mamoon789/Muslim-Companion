package com.iqra.alquran.utils

object Constants {
    //region keys for youtube & test ads
    const val VIDEO_ID_MECCA = "wkmIFbf_R_s"
    const val VIDEO_ID_MEDINA = "NfEglaLYDwc"
    const val APPOPEN_AD_ID = "ca-app-pub-3940256099942544/3419835294"
    const val INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"
    const val NATIVE_AD_ID = "ca-app-pub-3940256099942544/2247696110"
    const val BANNER_AD_ID = "ca-app-pub-3940256099942544/9214589741"
    //endregion

    //region global variable for ads
    var INTERSTITIAL_AD_SHOWN = false
    //endregion

    //region keys for error comparison
    const val MSG_CONNECT_INTERNET = "Connect to stable internet connection"
    const val MSG_TRY_LATER = "Something went wrong. Try again later"
    //endregion

    //region keys for alarm comparison
    val NAMAZ = listOf(
        "fajr",
        "dhuhr",
        "asr",
        "maghrib",
        "isha"
    )
    //endregion

    //region keys for intent & preferences
    const val KEY_SHOWCASE = "SHOWCASE"
    const val KEY_SCRIPT = "SCRIPT"
    const val KEY_SCRIPT_FONT = "SCRIPT_FONT"
    const val KEY_TRANSLATION = "TRANSLATION"
    const val KEY_TRANSLATION_FONT = "TRANSLATION_FONT"
    const val KEY_ZOOM = "ZOOM"
    const val KEY_QURAN_BOOKMARKS = "QURAN_BOOKMARKS"
    const val KEY_NAMAZ_ALARMS = "NAMAZ_ALARMS"
    const val KEY_LAST_READ = "LAST_READ"
    const val KEY_LAT = "LAT"
    const val KEY_LONG = "LONG"
    const val KEY_IS_SUBSCRIBED = "IS_SUBSCRIBED"
    const val KEY_SHOW_PREMIUM_DIALOG = "SHOW_PREMIUM_DIALOG"
    const val KEY_HADITH_BOOKMARKS = "HADITH_BOOKMARKS"
    const val KEY_TASBEEH_DHIKRS = "KEY_TASBEEH_DHIKRS"
    //endregion

    //region global variables for quran
    var CURRENT_SCRIPT = ""
    var CURRENT_SCRIPT_FONT = ""
    var CURRENT_TRANSLATION = ""
    var CURRENT_TRANSLATION_FONT = ""
    var CURRENT_ZOOM = 0
    //endregion

    //region map for quran settings
    val SCRIPTS = mapOf(
        "indopak" to "بِسۡمِ اللهِ الرَّحۡمٰنِ الرَّحِيۡم",
    )
    val TRANSLATIONS = mapOf(
        "en" to "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
        "ur" to "اللہ کے نام سےشروع جو نہایت مہربان ہمیشہ رحم فرمانےوالا ہے"
    )
    val AR_FONTS = mapOf(
        "font 5" to "Scheherazade",
        "font 8" to "font"
//        "font 1" to "Tajawal",
//        "font 2" to "Amiri",
//        "font 3" to "Almarai",
//        "font 4" to "Lateef",
//        "font 6" to "Harmattan",
//        "font 7" to "Mirza",
    )
    val EN_FONTS = mapOf(
        "font 1" to "Ubuntu",
        "font 2" to "Open Sans",
        "font 3" to "Poppins",
        "font 4" to "Merriweather",
        "font 5" to "Lora"
    )
    val UR_FONTS = mapOf(
        "font 1" to "Tajawal",
        "font 2" to "Amiri",
        "font 3" to "Almarai",
        "font 4" to "Lateef",
        "font 5" to "Scheherazade",
        "font 6" to "Harmattan",
        "font 7" to "Mirza",
        "font 8" to "font"
    )
    //endregion
}