package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.model.QuestionModel
import com.app.testik.domain.model.Result
import com.app.testik.domain.repository.TestRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class GetTestQuestionsUseCase @Inject constructor(
    private val testRepository: TestRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(testId: String): Result<List<QuestionModel>> =
        wrap(
            block = { testRepository.getTestQuestions(testId = testId) },
            mapper = { it!!.map { question -> question.toDomain() } }
        )
}