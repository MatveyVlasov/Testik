package com.app.testik.presentation.screen.createdtests.mapper

import com.app.testik.domain.model.TestModel
import com.app.testik.presentation.screen.createdtests.model.CreatedTestDelegateItem

fun TestModel.toCreatedTestItem() =
    CreatedTestDelegateItem(
        id = id,
        title = title,
        image = image
    )