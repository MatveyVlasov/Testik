package com.app.testik.presentation.model

import android.os.Parcelable
import com.app.testik.domain.model.QuestionType
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class QuestionDelegateItem(
    val id: String = "",
    val testId: String = "",
    val title: String = "",
    val description: String = "",
    val image: String = "",
    val type: QuestionType = QuestionType.SINGLE_CHOICE,
    val answers: List<AnswerDelegateItem> = emptyList(),
    val enteredAnswer: String = "",
    val pointsMax: Int = 1,
    val pointsEarned: Int = 0
) : DelegateAdapterItem, Parcelable {

    override fun id() = id

    override fun content(): Any = this
}


