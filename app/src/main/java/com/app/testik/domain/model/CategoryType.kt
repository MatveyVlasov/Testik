package com.app.testik.domain.model

import androidx.annotation.StringRes
import com.app.testik.R

enum class CategoryType(val title: String, @StringRes val description: Int) {
    MATH(title = "math", description = R.string.category_math),
    HISTORY(title = "history", description = R.string.category_history),
    OTHER(title = "other", description = R.string.category_other),
    NOT_SELECTED(title = "", description = R.string.empty)
}