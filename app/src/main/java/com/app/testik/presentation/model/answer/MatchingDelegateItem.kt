package com.app.testik.presentation.model.answer

import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.util.randomId

data class MatchingDelegateItem(
    override val id: String = randomId,
    override val text: String = "",
    val textMatching: String = "",
    val textCorrect: String = ""
) : AnswerDelegateItem(id = id, text = text)

fun MatchingDelegateItem.copyMatching(textMatching: String) = copy(textMatching = textMatching)

fun MatchingDelegateItem.copyCorrect(textCorrect: String) = copy(textCorrect = textCorrect)

