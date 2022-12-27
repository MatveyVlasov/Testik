package com.app.testik.domain.usecase

import com.app.testik.domain.mapper.toDto
import com.app.testik.domain.model.*
import com.app.testik.domain.repository.StorageRepository
import com.app.testik.domain.repository.TestRepository
import com.app.testik.domain.util.ResultWrapper
import com.app.testik.domain.util.ResultWrapperImpl
import com.app.testik.util.loadedFromServer
import javax.inject.Inject

class UpdateQuestionsUseCase @Inject constructor(
    private val testRepository: TestRepository,
    private val storageRepository: StorageRepository
) : ResultWrapper by ResultWrapperImpl() {

    suspend operator fun invoke(testId: String, questions: List<QuestionModel>): Result<Unit> {

        val newQuestions = questions.toMutableList()
        var hasImageErrors = false

        for ((index, question) in questions.withIndex()) {
            if (question.image.isEmpty() || question.image.loadedFromServer()) continue

            wrap (
                block = { storageRepository.uploadQuestionImage(testId = testId, questionId = question.id, image = question.image) },
                mapper = { it.toString() }
            ).onSuccess {
                newQuestions[index] = newQuestions[index].copy(image = it)
            }.onError {
                newQuestions[index] = newQuestions[index].copy(image = "")
                hasImageErrors = true
            }
        }

        return wrap(
            block = { testRepository.updateQuestions(testId = testId, data = newQuestions.toDto()) },
            mapper = { }
        ).onSuccess {
            if (hasImageErrors) return Result.Error("Some images were not saved due to an error")
        }
    }
}
