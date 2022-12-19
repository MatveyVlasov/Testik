package com.app.testik.presentation.dialog.testinfo.mapper

import com.app.testik.domain.model.TestPassedModel
import com.app.testik.presentation.dialog.testinfo.model.TestInfoDialogUIState

fun TestInfoDialogUIState.toDomain() =
    TestPassedModel(
        testId = id,
        title = title,
        image = image
    )