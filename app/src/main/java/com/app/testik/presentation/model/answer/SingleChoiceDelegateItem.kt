package com.app.testik.presentation.model.answer

import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.util.randomId

data class SingleChoiceDelegateItem(
    override val id: String = randomId,
    override val text: String = "",
    val isCorrect: Boolean = false,
    val isSelected: Boolean = false
) : AnswerDelegateItem(id = id, text = text) {

    companion object {
        const val TRUE_DEFAULT = "true_default"
        const val FALSE_DEFAULT = "false_default"
    }
}

