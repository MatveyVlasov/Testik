package com.app.testik.presentation.screen.testspassed.mapper

import com.app.testik.domain.model.TestPassedModel
import com.app.testik.presentation.screen.testspassed.model.TestPassedDelegateItem
import com.app.testik.util.toDate

fun TestPassedModel.toTestPassedItem() =
    TestPassedDelegateItem(
        recordId = recordId,
        title = title,
        image = image,
        date = timeFinished.toDate(),
        isFinished = isFinished,
        pointsMax = pointsMax,
        pointsEarned = pointsEarned,
        pointsCalculated = pointsCalculated
    )