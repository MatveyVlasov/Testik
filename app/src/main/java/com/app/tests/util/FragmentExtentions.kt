package com.app.tests.util

import android.app.Activity
import android.app.AlertDialog
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.annotation.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.app.tests.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar

fun Fragment.getDrawable(@DrawableRes drawableResId: Int) = ResourcesCompat.getDrawable(
    resources,
    drawableResId,
    null
)

fun Fragment.getColor(@ColorRes colorResId: Int) = resources.getColor(colorResId, null)

fun Fragment.getDimens(@DimenRes dimenResId: Int) = resources.getDimension(dimenResId)

fun Fragment.getTypeface(@FontRes fontResId: Int) =
    ResourcesCompat.getFont(requireContext(), fontResId)

fun Fragment.addBackPressedCallback(callback: () -> Unit) {
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { callback() }
}

fun Fragment.setupBottomNavigation(isVisible: Boolean) {
    requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigationView).isVisible = isVisible
}

fun Fragment.hideKeyboard() {
    activity?.currentFocus?.let { view ->
        val imm = context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }
}

fun Fragment.showSnackbar(
    message: String,
    duration: Int = Snackbar.LENGTH_LONG
) {
    Snackbar.make(requireView(), message, duration).show()
}

fun Fragment.showSnackbar(
    @StringRes message: Int,
    duration: Int = Snackbar.LENGTH_LONG
) = showSnackbar(
    message = getString(message),
    duration = duration
)

fun Fragment.showAlert(
    title: String,
    message: String,
    positive: String,
    negative: String = "",
    onPositiveClick: () -> Unit = {},
    onNegativeClick: () -> Unit = {},
    cancelable: Boolean = false
) {
    AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setMessage(message)
        .setPositiveButton(positive) { _, _ -> onPositiveClick() }
        .setNegativeButton(negative) { _, _ -> onNegativeClick() }
        .setCancelable(cancelable)
        .show()
}

fun Fragment.showAlert(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes positive: Int,
    @StringRes negative: Int = R.string.empty,
    onPositiveClick: () -> Unit = {},
    onNegativeClick: () -> Unit = {},
    cancelable: Boolean = false
) {
    showAlert(
        title = getString(title),
        message = getString(message),
        positive = getString(positive),
        negative = getString(negative),
        onPositiveClick = onPositiveClick,
        onNegativeClick = onNegativeClick,
        cancelable = cancelable
    )
}

fun Fragment.getStringOrNull(@StringRes res: Int?) = if (res == null) null else getString(res)