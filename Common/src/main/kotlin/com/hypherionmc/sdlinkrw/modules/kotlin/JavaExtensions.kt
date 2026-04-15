package com.hypherionmc.sdlinkrw.modules.kotlin

import com.hypherionmc.sdlink.util.EncryptionUtil
import com.hypherionmc.sdlink.util.translations.SDText

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
//endregion

//region Int Extensions
fun Int.Companion.random(min: Int, max: Int): Int {
    return ((Math.random() * (max - min)) + min).toInt()
}
//endregion