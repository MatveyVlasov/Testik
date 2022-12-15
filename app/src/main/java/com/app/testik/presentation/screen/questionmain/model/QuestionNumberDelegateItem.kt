package com.app.testik.presentation.screen.questionmain.model

import com.app.testik.util.delegateadapter.DelegateAdapterItem

data class QuestionNumberDelegateItem(
    val number: Int,
    val isSelected: Boolean = false
) : DelegateAdapterItem {

    override fun id() = number

    override fun content(): Any = this
}

fun Int.toQuestionNumber() = QuestionNumberDelegateItem(number = this)
