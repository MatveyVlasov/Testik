package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.*
import com.app.testik.domain.repository.TestPassedRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class UpdateAnswersUseCase @Inject constructor(
    private val testPassedRepository: TestPassedRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(testId: String, questions: List<QuestionModel>): Result<Unit> {
        return wrap(
            block = { testPassedRepository.updateQuestions(testId = testId, questions = questions.map { it.toDto() }) },
            mapper = { }
        )
    }
}
