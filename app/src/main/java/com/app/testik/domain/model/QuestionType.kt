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
    ),
    SHORT_ANSWER(
        title = "short answer",
        description = R.string.short_answer,
        instruction = R.string.short_answer_instruction
    ),
    MATCHING(
        title = "matching",
        description = R.string.matching,
        instruction = R.string.matching_instruction
    ),
    ORDERING(
        title = "ordering",
        description = R.string.ordering,
        instruction = R.string.ordering_instruction
    ),
    NUMBER(
        title = "number",
        description = R.string.number,
        instruction = R.string.number_instruction
    ),
    TRUE_FALSE(
        title = "true false",
        description = R.string.true_false,
        instruction = R.string.true_false_instruction
    )
}