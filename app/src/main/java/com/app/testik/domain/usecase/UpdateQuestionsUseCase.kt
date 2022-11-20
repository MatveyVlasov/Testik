package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.QuestionModel
import com.app.testik.domain.model.Result
import com.app.testik.domain.repository.TestRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class UpdateQuestionsUseCase @Inject constructor(
    private val testRepository: TestRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(testId: String, questions: List<QuestionModel>): Result<Unit> =
        wrap(
            block = { testRepository.updateQuestions(testId = testId, questions = questions.map { it.toDto() }) },
            mapper = { }
        )
}