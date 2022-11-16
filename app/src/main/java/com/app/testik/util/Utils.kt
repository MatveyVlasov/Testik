package com.app.testik.util

import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Patterns
import android.widget.ImageView
import android.widget.TextView
import com.app.testik.R
import com.app.testik.data.model.ApiResult
import com.app.testik.util.Constants.USERNAME_GOOGLE_DELIMITER
import com.google.android.gms.tasks.Task
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.tasks.await
import java.util.*

val randomId: String
    get() = UUID.randomUUID().toString()

val Int.px get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int?.orZero() = this ?: 0

fun Char.isDigitOrLatinLowercase() = this.isDigit() || this in 'a'..'z'

fun String.isUsername() = all { it.isDigitOrLatinLowercase() } && isNotBlank()

fun String.toUsername() = filter { c -> c.isLetterOrDigit() || c == USERNAME_GOOGLE_DELIMITER }.lowercase()

fun String.removeExtraSpaces() = trim().replace("\\s+".toRegex(), " ")

fun String.isEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches() && isNotBlank()

fun Uri?.toAvatar() = toString().takeWhile { it != '=' }

fun String.loadedFromServer() = startsWith("http")

fun Context.setAppLocale(language: String): Context {
    val locale = Locale(language)
    Locale.setDefault(locale)
    val config = resources.configuration
    config.setLocale(locale)
    config.setLayoutDirection(locale)
    return createConfigurationContext(config)
}

suspend fun<T> Task<T>.execute(): ApiResult<Unit> {
    await()
    return if (isSuccessful) ApiResult.Success()
    else ApiResult.Error(exception?.message)
}

fun MaterialToolbar.setupLanguageItem(
    color: Int? = null,
    onClick: () -> Unit = {},
) {
    menu.findItem(R.id.language).apply {
        setActionView(R.layout.item_language)

        actionView?.apply {
            findViewById<TextView>(R.id.tvLanguage).apply {
                text = Locale.getDefault().language.uppercase()
                if (color != null) setTextColor(color)
            }
            if (color != null) findViewById<ImageView>(R.id.ivLanguage).setColorFilter(color)
            setOnClickListener { onClick() }
        }
    }
}

fun isOnline(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        ?: return false

    val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        ?: return false

    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
        return true
    }
    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
        return true
    }
    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
        return true
    }

    return false
}