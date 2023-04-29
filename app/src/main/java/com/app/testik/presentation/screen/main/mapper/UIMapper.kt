package com.app.testik.presentation.screen.main.mapper

import com.app.testik.domain.model.TestModel
import com.app.testik.presentation.screen.main.model.TestDelegateItem

fun TestModel.toTestItem() =
    TestDelegateItem(
        id = id,
        title = title,
        image = image,
        isOpen = isOpen,
        isPasswordEnabled = isPasswordEnabled
    )