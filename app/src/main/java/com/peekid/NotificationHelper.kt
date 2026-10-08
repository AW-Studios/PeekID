package com.peekid

object NotificationHelper {

    val SUPPORTED_PACKAGES = listOf(
        "com.whatsapp",
        "com.whatsapp.w4b" // WhatsApp Business
    )

    fun isSupported(packageName: String): Boolean {
        return packageName in SUPPORTED_PACKAGES
    }

    fun sanitizeText(sender: String): String {
        return "$sender sent you a message"
    }
}
