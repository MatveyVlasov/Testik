package com.app.testik.presentation.model

import com.app.testik.domain.model.QuestionType
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import java.io.Serializable

data class QuestionDelegateItem(
    val id: String = "",
    val testId: String = "",
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val type: QuestionType = QuestionType.SINGLE_CHOICE,
    val answers: List<AnswerDelegateItem> = emptyList()
) : DelegateAdapterItem, Serializable {

    override fun id() = id

    override fun content(): Any = this
}


