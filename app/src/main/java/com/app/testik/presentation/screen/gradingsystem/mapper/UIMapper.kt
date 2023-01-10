package com.app.testik.presentation.screen.gradingsystem.mapper

import com.app.testik.domain.model.GradeModel
import com.app.testik.presentation.screen.gradingsystem.model.GradeDelegateItem
import com.app.testik.util.toIntOrZero

fun GradeModel.toGradeItem() =
    GradeDelegateItem(
        grade = grade,
        pointsFrom = pointsFrom.toString(),
        pointsTo = pointsTo.toString()
    )

fun GradeDelegateItem.toDomain() =
    GradeModel(
        grade = grade,
        pointsFrom = pointsFrom.toIntOrZero(),
        pointsTo = pointsTo.toIntOrZero()
    )