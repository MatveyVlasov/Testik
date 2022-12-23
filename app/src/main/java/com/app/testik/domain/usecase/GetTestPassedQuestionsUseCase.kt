package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.model.QuestionModel
import com.app.testik.domain.model.Result
import com.app.testik.domain.repository.TestPassedRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class GetTestPassedQuestionsUseCase @Inject constructor(
    private val testPassedRepository: TestPassedRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(recordId: String): Result<List<QuestionModel>> =
        wrap(
            block = { testPassedRepository.getTestQuestions(recordId = recordId) },
            mapper = { it!!.map { question -> question.toDomain() } }
        )
}