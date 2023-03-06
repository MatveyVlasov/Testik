package com.app.testik.domain.model

import androidx.annotation.StringRes
import com.app.testik.R

enum class CategoryType(val title: String, @StringRes val description: Int) {
    ALL(title = "_all", description = R.string.category_all),
    LANGUAGES(title = "languages", description = R.string.category_languages),
    HISTORY(title = "history", description = R.string.category_history),
    CULTURE(title = "culture", description = R.string.category_culture),
    MATH(title = "math", description = R.string.category_math),
    IT(title = "it", description = R.string.category_it),
    PHYSICS(title = "physics", description = R.string.category_physics),
    CHEMISTRY(title = "chemistry", description = R.string.category_chemistry),
    BIOLOGY(title = "biology", description = R.string.category_biology),
    GEOGRAPHY(title = "geography", description = R.string.category_geography),
    SPORT(title = "sport", description = R.string.category_sport),
    OTHER(title = "other", description = R.string.category_other),
    NOT_SELECTED(title = "", description = R.string.empty)
}