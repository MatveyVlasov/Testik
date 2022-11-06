package com.app.tests.util

fun Int?.orZero() = this ?: 0

fun String.removeWhiteSpaces() = filter { !it.isWhitespace() }

fun String.isUsername() = all { c -> c.isDigit() || c in 'a'..'z' }