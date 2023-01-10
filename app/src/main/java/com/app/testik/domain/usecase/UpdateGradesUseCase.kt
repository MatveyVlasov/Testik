package com.app.testik.domain.usecase

import com.app.testik.data.model.GradesDto
import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.*
import com.app.testik.domain.repository.TestRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class UpdateGradesUseCase @Inject constructor(
    private val testRepository: TestRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(testId: String, isGradesEnabled: Boolean, grades: List<GradeModel>): Result<Unit> =
        wrap(
            block = {
                testRepository.updateGrades(
                    testId = testId,
                    data = GradesDto(
                        isEnabled = isGradesEnabled,
                        grades = grades.map { it.toDto() }
                    )
                )
            },
            mapper = { }
        )
    }
