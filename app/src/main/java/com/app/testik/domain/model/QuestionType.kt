package com.app.testik.domain.model

import androidx.annotation.StringRes
import com.app.testik.R

enum class QuestionType(
    val title: String,
    @StringRes val description: Int,
    @StringRes val instruction: Int
) {
    SINGLE_CHOICE(
        title = "single choice",
        description = R.string.single_choice,
        instruction = R.string.single_choice_instruction
    ),
    MULTIPLE_CHOICE(
        title = "multiple choice",
        description = R.string.multiple_choice,
        instruction = R.string.multiple_choice_instruction
    )
}