package com.app.testik.presentation.model.answer

import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.util.randomId

data class MatchingRightDelegateItem(
    override val id: String = randomId,
    val textMatching: String = "",
    val textCorrect: String = ""
) : AnswerDelegateItem(id = id, text = textMatching)

