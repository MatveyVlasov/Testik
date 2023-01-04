package com.app.testik.presentation.screen.testpasseddetail.mapper

import com.app.testik.domain.model.TestPassedModel
import com.app.testik.presentation.screen.testpasseddetail.model.TestPassedDetailScreenUIState
import com.app.testik.util.getTimeDifference
import com.app.testik.util.toDate

fun TestPassedModel.toUIState() =
    TestPassedDetailScreenUIState(
        recordId = recordId,
        testId = testId,
        title = title,
        timeSpent = getTimeDifference(timeStarted, timeFinished),
        date = timeFinished.toDate(),
        isFinished = isFinished,
        pointsMax = pointsMax,
        pointsEarned = pointsEarned,
        pointsCalculated = pointsCalculated
    )