package com.app.testik.presentation.screen.testresults.mapper

import com.app.testik.domain.model.TestPassedModel
import com.app.testik.presentation.screen.testresults.model.TestResultsScreenUIState
import com.app.testik.util.getTimeDifference
import com.app.testik.util.toDate

fun TestPassedModel.toUIState() =
    TestResultsScreenUIState(
        recordId = recordId,
        testId = testId,
        title = title,
        timeSpent = getTimeDifference(timeStarted, timeFinished),
        date = timeFinished.toDate(),
        isResultsShown = isResultsShown,
        pointsMax = pointsMax,
        pointsEarned = pointsEarned,
        gradeEarned = gradeEarned
    )