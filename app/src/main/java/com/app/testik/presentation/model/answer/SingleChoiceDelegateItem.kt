package com.app.testik.presentation.model.answer

import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.util.randomId

data class SingleChoiceDelegateItem(
    override val id: String = randomId,
    override val text: String = "",
    val isCorrect: Boolean = false,
    val isSelected: Boolean = false
) : AnswerDelegateItem(id = id, text = text)

