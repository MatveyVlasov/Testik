package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDomain
import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.*
import com.app.testik.domain.repository.TestPassedRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import javax.inject.Inject

class SubmitQuestionUseCase @Inject constructor(
    private val testPassedRepository: TestPassedRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(
        recordId: String,
        question: QuestionModel,
        num: Int,
        isTimerFinished: Boolean
    ): Result<AnswerResultsModel> {
        return wrap(
            block = {
                testPassedRepository.submitQuestion(
                    recordId = recordId,
                    question = question.toDto(),
                    num = num,
                    isTimerFinished = isTimerFinished
                )
            },
            mapper = { it!!.toDomain() }
        )
    }
}
