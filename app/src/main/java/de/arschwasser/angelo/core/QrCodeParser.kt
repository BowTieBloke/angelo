package de.arschwasser.angelo.core

object QrCodeParser {
    fun extractCode(qr: String, pattern: String = AppConfig.QR_URL_PATTERN): String? {
        val normalizedQr = qr.trim()
        if (normalizedQr.isBlank()) return null

        val resolvedPattern = pattern
            .replace("\${BASE_URL}", AppConfig.BASE_URL)
            .replace("{BASE_URL}", AppConfig.BASE_URL)
        val marker = "{code}"
        val markerIndex = resolvedPattern.indexOf(marker)
        if (markerIndex == -1) return normalizedQr

        val prefix = resolvedPattern.substring(0, markerIndex)
        val suffix = resolvedPattern.substring(markerIndex + marker.length)
        if (!normalizedQr.startsWith(prefix)) return normalizedQr

        val codeWithTail = normalizedQr.removePrefix(prefix)
        val code = if (suffix.isBlank()) {
            codeWithTail.substringBefore('&')
        } else {
            codeWithTail.substringBefore(suffix)
        }
        return code.takeIf { it.isNotBlank() }
    }
}
