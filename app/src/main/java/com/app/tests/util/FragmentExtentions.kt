package com.app.tests.util

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.annotation.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
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