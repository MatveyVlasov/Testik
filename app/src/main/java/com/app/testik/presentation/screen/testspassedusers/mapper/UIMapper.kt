package com.app.testik.presentation.screen.testspassedusers.mapper

import com.app.testik.domain.model.TestPassedModel
import com.app.testik.presentation.screen.testspassedusers.model.TestPassedUserDelegateItem
import com.app.testik.util.toDate

fun TestPassedModel.toTestPassedUserItem() =
    TestPassedUserDelegateItem(
        recordId = recordId,
        user = user,
        title = title,
        date = timeFinished.toDate(),
        isFinished = isFinished,
        pointsMax = pointsMax,
        pointsEarned = pointsEarned,
        pointsCalculated = pointsCalculated,
        gradeEarned = gradeEarned
    )