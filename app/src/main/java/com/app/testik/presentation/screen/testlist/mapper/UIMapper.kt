package com.app.testik.presentation.screen.testlist.mapper

import com.app.testik.domain.model.TestModel
import com.app.testik.presentation.screen.testlist.model.TestInfoDelegateItem

fun TestModel.toTestInfoItem() =
    TestInfoDelegateItem(
        id = id,
        title = title,
        image = image,
        category = category.description,
        isOpen = isOpen,
        isPasswordEnabled = isPasswordEnabled
    )