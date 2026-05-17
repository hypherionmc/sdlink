package com.hypherionmc.sdlinkrw.modules.kotlin.java_ext

import com.hypherionmc.sdlinkrw.modules.translations.SDText
import com.hypherionmc.sdlinkrw.util.EncryptionUtil
import org.apache.commons.lang3.RandomStringUtils

//region String Extensions
fun String.translate(): String {
    return SDText.translate(this).toString()
}

fun String.encrypt(): String {
    return EncryptionUtil.INSTANCE.encrypt(this)
}

fun String.decrypt(): String {
    return EncryptionUtil.INSTANCE.decrypt(this)
}

fun String.Companion.saltString(): String {
    return RandomStringUtils.random(Int.random(30, 100), true, true)
}
//endregion

//region Int Extensions
fun Int.Companion.random(min: Int, max: Int): Int {
    return ((Math.random() * (max - min)) + min).toInt()
}
//endregion

//region Long Extensions
/**
 * Convert a long value to a timestamp string.
 *
 * @return A string representation of the timestamp.
 */
fun Long.toTimestamp(): String {
    val seconds: Long = this % 60
    val minutes: Long = (this / 60) % 60
    val hours: Long = (this / 3600) % 24
    val days: Long = this / (3600 * 24)

    var timeString = String.format("%02d hour(s), %02d minute(s), %02d second(s)", hours, minutes, seconds)

    if (days > 0) {
        timeString = String.format("%d day(s), %s", days, timeString)
    }

    return timeString
}

/**
 * Convert a long value to a human-readable string with appropriate units.
 *
 * @return A string representation of the value in the appropriate units.
 */
fun Long.toHumanReadable(): String {
    if (this < 1024) return "$this B"

    val units = arrayOf("KB", "MB", "GB", "TB", "PB")
    var value = this.toDouble()
    var unitIndex = -1

    do {
        value /= 1024
        unitIndex++
    } while (value >= 1024 && unitIndex < units.lastIndex)

    return String.format("%.2f %s", value, units[unitIndex])
}
//endregion

//region Collection Extensions
fun <T> List<T>.listBatches(length: Int): List<List<T>> {
    require(length > 0) { "length = $length" }
    return this.chunked(length)
}
//endregion