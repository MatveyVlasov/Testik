package com.app.testik.presentation.model.answer

import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.util.randomId

data class ShortAnswerDelegateItem(
    override val id: String = randomId,
    override val text: String = ""
) : AnswerDelegateItem(id = id, text = text)

