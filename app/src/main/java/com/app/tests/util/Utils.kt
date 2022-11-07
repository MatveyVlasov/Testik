package com.app.tests.util

import android.util.Patterns
import com.app.tests.util.Constants.USERNAME_GOOGLE_DELIMITER

fun Int?.orZero() = this ?: 0

fun Char.isDigitOrLatinLowercase() = this.isDigit() || this in 'a'..'z'

fun String.isUsername() = all { it.isDigitOrLatinLowercase() } && isNotBlank()

fun String.toUsername() = filter { c -> c.isDigitOrLatinLowercase() || c == USERNAME_GOOGLE_DELIMITER }.lowercase()

fun String.isEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches() && isNotBlank()