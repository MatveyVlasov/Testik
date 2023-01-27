package com.app.testik.presentation.model.answer

import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.util.randomId

data class OrderingDelegateItem(
    override val id: String = randomId,
    override val text: String = "",
    val textCorrect: String = ""
) : AnswerDelegateItem(id = id, text = text)

fun OrderingDelegateItem.copyCorrect(textCorrect: String) = copy(textCorrect = textCorrect)

