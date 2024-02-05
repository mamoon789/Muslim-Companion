package com.iqra.alquran.utils

import android.content.res.Resources

object Constants {
    //region keys for maps, youtube, inapp & ads
    const val API_KEY = "AIzaSyAe8ulK5nG2H1-KDWp8g7qct06Oq7jY8m8"
    const val VIDEO_ID_MECCA = "HurieDZm_fs"
    const val VIDEO_ID_MEDINA = "gUC3TjCrwRw"
    const val LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjTZp7dH4dSBrykOX4XWl8zSxTq9eTR5jdUGzGvtQL4d6NieXqvo3rsFqa7ZPZbFo8EFaBbGjQlD+meWQnLdXO6ZJlMwpiZ0sctRZ0/Z5jgSbEDN7YKbkNWbPhBcyqFwHmyiP+SRSR4vAy4Fc/9KqHH78b6ddJ5wVGnjF5sakhU0+Sr6MqER3M15Dtjv/sSgzdGRiJ1zcj0JG8mrljhqayt4rC6HhZM7cJUOkA/AiG4VMNVXZRei7TTMbKvBmdMErwy+KuWvOBUv/K1ybiTFGO38fP+vY/OGfX27r6YNZbFT8WkCEHGrC6DAU2Xia9HAAQ4CN4cH5IF2G6cmCY528mQIDAQAB\n"
    const val PRODUCT_ID = "sub101"
    const val INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"     // Test Ad Id
//    const val INTERSTITIAL_AD_ID = "ca-app-pub-1035415808955400/6606621748"     // Live Ad Id
    const val APPOPEN_AD_ID = "ca-app-pub-3940256099942544/3419835294"     // Test Ad Id
//    const val APPOPEN_AD_ID = "ca-app-pub-1035415808955400/8170562075"     // Live Ad Id
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
    const val KEY_SCRIPT = "SCRIPT"
    const val KEY_SCRIPT_FONT = "SCRIPT_FONT"
    const val KEY_TRANSLATION = "TRANSLATION"
    const val KEY_TRANSLATION_FONT = "TRANSLATION_FONT"
    const val KEY_ZOOM = "ZOOM"
    const val KEY_BOOKMARKS = "BOOKMARKS"
    const val KEY_NAMAZ_ALARMS = "NAMAZ_ALARMS"
    const val KEY_LAST_READ = "LAST_READ"
    const val KEY_LAT = "LAT"
    const val KEY_LONG = "LONG"
    const val KEY_IS_SUBSCRIBED = "IS_SUBSCRIBED"
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
        "uthmani" to "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيم"
    )
    val TRANSLATIONS = mapOf(
        "en" to "In the name of Allah, the Entirely Merciful, the Especially Merciful.",
        "ur" to "اللہ کے نام سےشروع جو نہایت مہربان ہمیشہ رحم فرمانےوالا ہے"
    )
    val AR_FONTS = mapOf(
        "font 5" to "Scheherazade",
        "font 8" to "indopak_font"
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
        "font 8" to "indopak_font"
    )
    //endregion
}