package com.app.testik.domain.model

import androidx.annotation.StringRes
import com.app.testik.R

enum class QuestionType(val title: String, @StringRes val description: Int) {
    SINGLE_CHOICE(title = "single choice", description = R.string.single_choice),
    MULTIPLE_CHOICE(title = "multiple choice", description = R.string.multiple_choice)
}