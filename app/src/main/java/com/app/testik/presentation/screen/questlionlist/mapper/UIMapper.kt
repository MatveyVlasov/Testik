package com.app.testik.presentation.screen.questlionlist.mapper

import com.app.testik.domain.model.QuestionModel
import com.app.testik.presentation.screen.questlionlist.model.QuestionDelegateItem

fun QuestionModel.toQuestionItem() =
    QuestionDelegateItem(
        id = id,
        title = title,
        image = image
    )