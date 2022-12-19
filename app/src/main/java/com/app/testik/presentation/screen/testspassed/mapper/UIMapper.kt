package com.app.testik.presentation.screen.testspassed.mapper

import com.app.testik.domain.model.TestPassedModel
import com.app.testik.presentation.screen.testspassed.model.TestPassedDelegateItem

fun TestPassedModel.toTestPassedItem() =
    TestPassedDelegateItem(
        recordId = recordId,
        title = title,
        image = image
    )