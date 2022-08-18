package com.dillon.supercam.key

/**
 * Created by dillon on 2017/6/11.
 */

object CommonK {
    val CHANNEL_ID = "Android"
    val CHANNEL_NAME = "System services"

    val Type_Camera_Front = "front"
    val Type_Camera_Back = "back"

    val Type_Media_Video = "video"
    val Type_Media_Photo = "photo"
    val Type_Media_Audio = "audio"

    val Type_Remind_None = "none"
    val Type_Remind_Start = "start"
    val Type_Remind_Stop = "stop"
    val Type_Remind_Both = "both"

    val Type_Screen_On = "Screen_On"
    val Type_Screen_Off = "Screen_Off"
    val Type_Screen_Present = "Screen_Present"
    val Type_Home_Key = "Home_Key"
    val Type_Recent_Key = "Recent_Key"
    val Type_Volume_Key = "Volume_Key"

    val Type_size_1080 = "1080P"
    val Type_size_2K = "2K"
    val Type_size_4K = "4K"
    val Type_size_Best = "best"

    val Task_single = "task_single"
    val Task_hourly = "task_hourly"
    val Task_daily = "task_daily"

    val Mode_silent = "silent_mode"
    val Mode_sound = "sound_mode"
    val Mode_vibrate = "vibrate_mode"

    val Mode_ai = "mode_ai"
    val Mode_sc = "mode_capture"


    val proPkg = "com.dillon.supercam"
    val BUGLY_APP_ID = ""

    val devEmail = "uwk6tqiuoHivqpk6~ml6tkl"
    val devPass = ">>2tkl"


    val Front = "Front"
    val Back = "Back"

    var CHAT_TARGET = mutableListOf(
        "jp.naver.line.android",
        "com.facebook.mlite",
        "com.google.android.talk",
        "com.nhn.android.band",
        "com.discord",
        "com.skype.raider",
        "com.facebook.orca",
        "kik.android",
        "com.Slack",
        "com.snapchat.android",
        "org.telegram.messenger",
        "com.viber.voip",
        "com.whatsapp",
        "com.tencent.mm",
        "com.tencent.mobileqq",
        "com.instagram.android",
        "com.google.android.gm",
        "com.zhiliaoapp.musically",
        "com.facebook.katana"

    )
    var BROWSER_TARGET = mutableListOf(
        "org.mozilla.firefox",
        "com.opera.browser",
        "com.ksmobile.cb",
        "com.android.chrome",
        "com.UCMobile.intl",
        "com.sec.android.app.sbrowser"

    )

    val PUBLIC_KEY =
        "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCundLhbxXpqVIxPnKURadFbvDqtOrkriREKNYMQzRUwRtZ/fe7qcui7vvnmBJeMq3ts85B6U0CUTL/eABbHvEdPVuZP1n/nH9h2jc4fAxuaDPvbgR/YYKSWNEzQ6oiq58FlzL4nYaTXeWFjjSgWAZRJN+DmdlxUsiKfpNx9PhL+QIDAQAB"
    val PRIVATE_KEY =
        "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAK6d0uFvFempUjE+cpRFp0Vu8Oq06uSuJEQo1gxDNFTBG1n997upy6Lu++eYEl4yre2zzkHpTQJRMv94AFse8R09W5k/Wf+cf2HaNzh8DG5oM+9uBH9hgpJY0TNDqiKrnwWXMvidhpNd5YWONKBYBlEk34OZ2XFSyIp+k3H0+Ev5AgMBAAECgYAz+s1MyFm4jevmttU66CUsGSNkaujFnEU1eQaG7faFCFsRBfYaCiiRXxzjyzQkkGeQLAdJPZ7sAqnwvJM2jNZvRAq7XM8mInwIj6kNJchuORjd+S7+2vFEJ9ruwqfTK/i+13aqwJyfXX53jl9JbmQTb0AFdaFMMP5vZgeb90fYhQJBAO6BwKCrgA2HyT2Xv4fN/fRpxHajC5KGG0hrAKEw+PwMr1gvdJDjLBEzKERwKcv3PdfMx9/FKC6vTqLvEjOz758CQQC7bHRiwlpSlweW/6WwjWHNEla3BFZMMjZN/lCVtfx7kdPcmqQJmicXZZKr0XTig5o0oj2uGhaJxhzsR+b+Rj1nAkAZW8JXUuSyi5Vh7xh2H/i4W+Z/lqZMVeXgtT/D46kVY2PhRGpoXT76NS462JIZFZiFsUgvCo9TJ2B9Al41ERSRAkBhLHMPfUsNRSb3QCbs6fDKPXbePw5rRSFowLGRXaDBhLM+zqK8I8Oe2tf344phovAB2Bh4uyfyWNhIHWODdHGDAkBthhsS2E2x6K8M3dL/QULojSLLBT7o0Hyo2BjCIUTe1/48KoJWgXwmAWbI293/LupofEWhhc44GWNBeXCzB+gp"

    var REQUSTCODE_HOME_BG = 1
    var REQUSTCODE_SETTING_BG = 2
    var REQUSTCODE_FAQ_BG = 3
    var REQUSTCODE_PASS_BG = 4
    var REQUSTCODE_THEME_BG = 5
    var REQUSTCODE_CHAT_BG = 6

    var PAGE_HOME = "page_home"
    var PAGE_CAPTURE = "page_capture_set"
    var PAGE_SETTING = "page_setting"
    var PAGE_FAQ = "page_faq"
    var PAGE_PASS = "page_pass"
    var PAGE_THEME = "page_theme"
    var PAGE_PAY = "page_pay"
    var PAGE_CHAT = "page_chat"

    val AD_BANNER_HOME = "ca-app-pub-4382533706017903/1615371401"
    val AD_BANNER_PASS = "ca-app-pub-4382533706017903/3858391361"
    val AD_BANNER_SETTING = "ca-app-pub-4382533706017903/9110718045"

    var requestCaptureSingle = 1288
    var requestCaptureHourly= 1289
    var requestCaptureDaily= 1290

    var shortCutType="shortCutType"
    var shortCutPhoto="shortCutPhoto"
    var shortCutVideo="shortCutVideo"
    var shortCutAudio="shortCutAudio"
    var shortCutEnter="shortCutEnter"
    var officialWebsitePro="https://play.google.com/store/apps/details?id=com.dillon.supercampro"
    var officialWebsite="https://play.google.com/store/apps/details?id=com.dillon.supercam"

    var MSG_TYPE_TEXT = "text"
    var MSG_TYPE_IMG = "image"
    var MSG_TYPE_AUDIO = "audio"
    var MSG_TYPE_VIDEO = "video"

}
