package com.app.testik.presentation.model

import com.app.testik.presentation.screen.questionedit.model.MultipleChoiceDelegateItem
import com.app.testik.presentation.screen.questionedit.model.SingleChoiceDelegateItem
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import com.app.testik.util.randomId
import java.io.Serializable

open class AnswerDelegateItem(
    open val id: String = randomId,
    open val text: String = ""
) : DelegateAdapterItem, Serializable {

    override fun id() = id

    override fun content(): Any = this
}

fun AnswerDelegateItem.copy(text: String): AnswerDelegateItem {
    return when (this) {
        is SingleChoiceDelegateItem -> copy(text = text)
        is MultipleChoiceDelegateItem -> copy(text = text)
        else -> AnswerDelegateItem(id = id, text = text)
    }
}


