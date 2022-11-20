package com.app.testik.util

import android.app.Activity
import android.app.AlertDialog
import android.view.inputmethod.InputMethodManager
import androidx.activity.addCallback
import androidx.annotation.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.app.testik.R
import com.app.testik.util.Constants.CATEGORIES
import com.app.testik.util.Constants.LANGUAGES
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

fun Fragment.showExitAlert() {
    showAlert(
        title = R.string.app_exit,
        message = R.string.exit_app_confirmation,
        positive = R.string.exit_app,
        negative = R.string.cancel,
        onPositiveClick = { requireActivity().finish() }
    )
}

fun Fragment.confirmExitWithoutSaving() {
    showAlert(
        title = R.string.go_back,
        message = R.string.go_back_confirmation,
        positive = R.string.confirm,
        negative = R.string.cancel,
        onPositiveClick = { findNavController().navigateUp() }
    )
}

fun Fragment.showSingleChoiceDialog(
    title: String,
    positive: String,
    negative: String,
    items: List<String>,
    selectedItem: Int = 0,
    onPositiveClick: () -> Unit = {},
    onNegativeClick: () -> Unit = {},
    onItemClick: (Int) -> Unit = {}
) {
    AlertDialog.Builder(requireContext())
        .setTitle(title)
        .setSingleChoiceItems(items.toTypedArray(), selectedItem) { _, which ->
            onItemClick(which)
        }
        .setPositiveButton(positive) { _, _ -> onPositiveClick() }
        .setNegativeButton(negative) { _, _ -> onNegativeClick() }
        .show()
}

fun Fragment.showSingleChoiceDialog(
    @StringRes title: Int,
    @StringRes positive: Int,
    @StringRes negative: Int,
    items: List<Int>,
    selectedItem: Int = 0,
    onPositiveClick: () -> Unit = {},
    onNegativeClick: () -> Unit = {},
    onItemClick: (Int) -> Unit = {}
) {
    showSingleChoiceDialog(
        title = getString(title),
        positive = getString(positive),
        negative = getString(negative),
        items = items.map { getString(it) },
        selectedItem = selectedItem,
        onPositiveClick = onPositiveClick,
        onNegativeClick = onNegativeClick,
        onItemClick = onItemClick
    )
}

fun Fragment.showChangeLanguageDialog(onSelected: (String) -> Unit) {
    var selectedItem = -1

    showSingleChoiceDialog(
        title = R.string.select_language_translated,
        positive = R.string.select_translated,
        negative = R.string.cancel_translated,
        items = LANGUAGES.values.toList(),
        selectedItem = selectedItem,
        onPositiveClick = {
            if (selectedItem != -1) onSelected(LANGUAGES.keys.elementAt(selectedItem))
        },
        onItemClick = { selectedItem = it }
    )
}

fun Fragment.getStringOrNull(@StringRes res: Int?) = if (res == null) null else getString(res)

fun Fragment.getCategory(str: String) = getStringOrNull(CATEGORIES[str])