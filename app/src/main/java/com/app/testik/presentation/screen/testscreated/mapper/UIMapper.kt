package com.app.testik.presentation.screen.testscreated.mapper

import com.app.testik.domain.model.TestModel
import com.app.testik.presentation.screen.testscreated.model.TestCreatedDelegateItem

fun TestModel.toTestCreatedItem() =
    TestCreatedDelegateItem(
        id = id,
        title = title,
        image = image
    )