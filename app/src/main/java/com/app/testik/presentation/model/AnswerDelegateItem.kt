package com.app.testik.presentation.model

import android.os.Parcelable
import com.app.testik.presentation.model.answer.MatchingDelegateItem
import com.app.testik.presentation.model.answer.MultipleChoiceDelegateItem
import com.app.testik.presentation.model.answer.ShortAnswerDelegateItem
import com.app.testik.presentation.model.answer.SingleChoiceDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import com.app.testik.util.randomId
import kotlinx.parcelize.Parcelize

@Parcelize
open class AnswerDelegateItem(
    open val id: String = randomId,
    open val text: String = ""
) : DelegateAdapterItem, Parcelable {

    override fun id() = id

    override fun content(): Any = this
}

fun AnswerDelegateItem.copy(text: String): AnswerDelegateItem {
    return when (this) {
        is SingleChoiceDelegateItem -> copy(text = text)
        is MultipleChoiceDelegateItem -> copy(text = text)
        is ShortAnswerDelegateItem -> copy(text = text)
        is MatchingDelegateItem -> copy(text = text)
        else -> AnswerDelegateItem(id = id, text = text)
    }
}

fun AnswerDelegateItem.copy(isCorrect: Boolean): AnswerDelegateItem {
    return when (this) {
        is SingleChoiceDelegateItem -> copy(isCorrect = isCorrect)
        is MultipleChoiceDelegateItem -> copy(isCorrect = isCorrect)
        is ShortAnswerDelegateItem -> copy()
        is MatchingDelegateItem -> copy()
        else -> AnswerDelegateItem(id = id, text = text)
    }
}


