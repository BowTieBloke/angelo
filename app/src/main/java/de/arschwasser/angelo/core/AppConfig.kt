package de.arschwasser.angelo.core

object AppConfig {
    var BASE_URL = "https://angelo.arschwasser.de"
    const val CSV_LIST_PATH = "/csv/list.json"
    const val UPDATE_JSON = "/app/update.json"
    var CONNECT_TIMEOUT_MS = 3_000
    var READ_TIMEOUT_MS = 8_000
    var QR_URL_PATTERN = "\${BASE_URL}/game?c={code}"
    const val SPOTIFY_CLIENT_ID = "d3dcbf1f102c4b968f8c69c97c22f7f2"
    const val SPOTIFY_REDIRECT_URI = "angelo://auth"
    var ALLOW_SCREEN_FLASH = true
}
