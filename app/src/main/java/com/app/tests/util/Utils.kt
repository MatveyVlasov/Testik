package com.app.tests.util

fun Int?.orZero() = this ?: 0

fun Char.isDigitOrLatinLowercase() = this.isDigit() || this in 'a'..'z'

fun String.isUsername() = all { it.isDigitOrLatinLowercase() }

fun String.toUsername() = filter { c -> c.isDigitOrLatinLowercase() }.lowercase()