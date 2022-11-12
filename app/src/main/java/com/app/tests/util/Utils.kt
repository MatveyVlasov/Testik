package com.app.tests.util

import android.content.res.Resources
import android.net.Uri
import android.util.Patterns
import com.app.tests.util.Constants.USERNAME_GOOGLE_DELIMITER

val Int.px get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int?.orZero() = this ?: 0

fun Char.isDigitOrLatinLowercase() = this.isDigit() || this in 'a'..'z'

fun String.isUsername() = all { it.isDigitOrLatinLowercase() } && isNotBlank()

fun String.toUsername() = filter { c -> c.isLetterOrDigit() || c == USERNAME_GOOGLE_DELIMITER }.lowercase()

fun String.removeExtraSpaces() = trim().replace("\\s+".toRegex(), " ")

fun String.isEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches() && isNotBlank()

fun Uri?.toAvatar() = toString().takeWhile { it != '=' }

fun String.loadedFromServer() = startsWith("http")