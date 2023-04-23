package com.app.testik.domain.mapper

import com.app.testik.data.model.*
import com.app.testik.data.model.TestDto
import com.app.testik.domain.model.*

fun UserDto.toDomain() =
    UserModel(
        uid = uid,
        email = email,
        username = username,
        firstName = firstName,
        lastName = lastName,
        avatar = avatar
    )

fun TestsDto.toDomain(): TestsModel {
    val tests = mutableListOf<TestDto>()
    if (!snapshot.isEmpty) {
        for (document in snapshot) {
            val data = document.toObject(TestDto::class.java)
            tests.add(data.copy(id = document.id))
        }
    }

    return TestsModel(
        snapshot = snapshot,
        tests = tests.map { it.toDomain() }
    )
}

fun TestsPassedDto.toDomain(): TestsPassedModel {
    val tests = mutableListOf<TestPassedDto>()
    if (!snapshot.isEmpty) {
        for (document in snapshot) {
            val data = document.toObject(TestPassedDto::class.java)
            tests.add(data.copy(recordId = document.id))
        }
    }

    return TestsPassedModel(
        snapshot = snapshot,
        tests = tests.map { it.toDomain() }
    )
}

fun TestCreationDto.toDomain() =
    TestCreationModel(
        id = id,
        link = link
    )

fun TestDto.toDomain() =
    TestModel(
        id = id,
        author = author,
        title = title,
        description = description,
        category = category.toCategoryType(),
        image = image,
        isPasswordEnabled = isPasswordEnabled,
        isPublished = isPublished,
        isLinkEnabled = isLinkEnabled,
        link = link,
        isResultsShown = isResultsShown,
        isCorrectAnswersShown = isCorrectAnswersShown,
        isCorrectAnswersAfterQuestionShown = isCorrectAnswersAfterQuestionShown,
        isNavigationEnabled = isNavigationEnabled,
        isRandomQuestions = isRandomQuestions,
        isRandomAnswers = isRandomAnswers,
        questionsNum = questionsNum,
        pointsMax = pointsMax
    )

fun TestPassedDto.toDomain() =
    TestPassedModel(
        recordId = recordId,
        testId = testId,
        title = title,
        image = image,
        user = user,
        timeStarted = timeStarted,
        timeFinished = timeFinished,
        isResultsShown = isResultsShown,
        isNavigationEnabled = isNavigationEnabled,
        isFinished = isFinished,
        pointsMax = pointsMax,
        pointsEarned = pointsEarned,
        pointsCalculated = pointsCalculated,
        gradeEarned = gradeEarned,
        isDemo = isDemo
    )

fun TestQuestionsDto.toDomain() =
    questions.mapIndexed { index, item ->
        item.toDomain(answersCorrect = answersCorrect[index].answers, explanation = explanations[index])
    }

fun QuestionDto.toDomain(
    answersCorrect: List<AnswerCorrectDto>? = null,
    explanation: String = ""
): QuestionModel {
    val type = type.toQuestionType()

    val answers = when(type) {
        QuestionType.SHORT_ANSWER -> {
            answersCorrect?.map {
                AnswerModel(
                    type = QuestionType.SHORT_ANSWER,
                    text = it.text,
                    textMatching = it.textMatching
                )
            } ?: emptyList()
        }
        else -> {
            answers.mapIndexed { index, item ->
                item.toDomain(
                    type = type,
                    isCorrect = answersCorrect?.get(index)?.isCorrect ?: false
                )
            }
        }
    }

    val correctNumber = if (type == QuestionType.NUMBER && answersCorrect?.isNotEmpty() == true) answersCorrect[0].text.toDouble()
                        else 0.0

    return QuestionModel(
        id = id,
        testId = testId,
        title = title,
        description = description,
        explanation = explanation,
        image = image,
        type = type,
        isRequired = isRequired,
        answers = answers,
        enteredAnswer = enteredAnswer,
        isMatch = isMatch,
        isCaseSensitive = isCaseSensitive,
        correctNumber = correctNumber,
        percentageError = percentageError,
        pointsMax = pointsMax,
        pointsEarned = pointsEarned
    )
}

fun AnswerDto.toDomain(type: QuestionType, isCorrect: Boolean) =
    AnswerModel(
        type = type,
        text = text,
        textMatching = textMatching,
        isCorrect = isCorrect,
        isSelected = isSelected
    )

fun AnswerCorrectDto.toDomain() =
    AnswerModel(
        text = text,
        textMatching = textMatching,
        isCorrect = isCorrect
    )

fun ResultsDto.toDomain() =
    ResultsModel(
        answersCorrect = answersCorrect.map { it.answers.map { answer -> answer.toDomain() } },
        pointsPerQuestion = pointsPerQuestion,
        explanations = explanations
    )

fun String.toCategoryType() =
    CategoryType.values().find { it.title == this } ?: CategoryType.NOT_SELECTED

fun String.toQuestionType() =
    QuestionType.values().find { it.title == this } ?: QuestionType.SINGLE_CHOICE

fun GradesDto.toDomain() =
    GradesModel(
        isEnabled = isEnabled,
        grades = grades.map { it.toDomain() }
    )

fun GradeDto.toDomain() =
    GradeModel(
        grade = grade,
        pointsFrom = pointsFrom,
        pointsTo = pointsTo
    )

fun PointsEarnedDto.toDomain() =
    PointsEarnedModel(
        pointsEarned = pointsEarned,
        gradeEarned = gradeEarned
    )

fun AnswerResultsDto.toDomain() =
    AnswerResultsModel(
        points = points,
        pointsEarned = pointsEarned,
        answersCorrect = answersCorrect?.answers?.map { answer -> answer.toDomain() } ?: emptyList(),
        explanation = explanation
    )
