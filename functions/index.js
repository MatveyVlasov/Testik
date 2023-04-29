const functions = require("firebase-functions")

const admin = require('firebase-admin')
admin.initializeApp()

const db = admin.firestore()

// exports.addQuestionsToTestPassed = functions.firestore
//     .document('testsPassed/{test}')
//     .onCreate(async (snap, context) => {
//         const data = snap.data()

//         const test = await db.collection("tests").doc(data.testId).collection("private").doc("questions").get()
//         const questions = test.data().questions
//         const newQuestions = []

//         for (let i = 0; i < questions.length; ++i) {
//             const q = questions[i]
//             q.answers = q.answers.map((ans) => ({
//                 ...ans,
//                 isCorrect: false,
//             }))
//             newQuestions.push(questions[i])
//         }

//         return snap.ref.set({
//             questions: newQuestions,
//         }, { merge: true })
//     })


exports.startTest = functions
    .runWith({
        enforceAppCheck: true,
    })
    .https.onCall((data, context) => {

        return new Promise((resolve, reject) => {

            if (context.app == undefined) {
                return reject(new functions.https.HttpsError('failed-precondition', 'App not verified'))
            }

            if (context.auth == null) {
                return reject(new functions.https.HttpsError('unauthenticated', 'Not logged in'))
            }

            const testId = data.testId || null
            const isDemo = data.isDemo
            const passwordEntered = data.password

            const testRef = db.collection("tests").doc(testId)

            testRef.get().then((doc) => {
                if (!doc.exists) {
                    return reject(new functions.https.HttpsError('not-found', 'Test not found'))
                }

                const testData = doc.data()
                const isOpen = testData.isOpen || false

                if (!isOpen) {
                    return reject(new functions.https.HttpsError('failed-precondition', 'Test closed'))
                }

                if (isDemo && testData.author != context.auth.uid) {
                    return reject(new functions.https.HttpsError('permission-denied', 'No access'))
                }

                const isGradesEnabled = testData.isGradesEnabled || false
                const grades = testData.grades || []
                const isResultsShown = testData.isResultsShown || false
                const isCorrectAnswersAfterQuestionShown = testData.isCorrectAnswersAfterQuestionShown || false
                const isRetakingEnabled = testData.isRetakingEnabled || false
                const isNavigationEnabled = testData.isNavigationEnabled
                const isRandomQuestions = testData.isRandomQuestions
                const isRandomAnswers = testData.isRandomAnswers

                testRef.collection("private").doc("password").get().then((docPassword) => {
                    const password = docPassword.data().password

                    if (password.length > 0 && password != passwordEntered) {
                        return reject(new functions.https.HttpsError('permission-denied', 'Incorrect password'))
                    }

                    testRef.collection("private").doc("questions").get().then((docQuestions) => {
                        const data = docQuestions.data()
                        const questions = data.questions
                        const answersCorrect = data.answersCorrect
                        const explanations = data.explanations

                        if (questions.length == 0) {
                            return reject(new functions.https.HttpsError('failed-precondition', 'No questions'))
                        }

                        try {
                            for (let i = 0; i < grades.length; ++i) {
                                if (!validateGradeFields(grades[i])) throw new Error
                            }
                            for (let i = 0; i < questions.length; ++i) {
                                if (!validateQuestionFields(questions[i])) throw new Error
                            }
                            for (let i = 0; i < explanations.length; ++i) {
                                if (!isString(explanations[i])) throw new Error
                            }
                        } catch (err) {
                            return reject(new functions.https.HttpsError('failed-precondition', 'Invalid data type'))
                        }

                        if (isRandomQuestions) shuffle(questions, answersCorrect, explanations)

                        for (let i = 0; i < questions.length; ++i) {
                            switch (questions[i].type) {
                            case 'matching': {
                                shuffleMatchingQuestion(questions[i])
                                break
                            }
                            case 'ordering': {
                                shuffleOrderingQuestion(questions[i])
                                break
                            }
                            case 'single choice':
                            case 'multiple choice': {
                                if (isRandomAnswers) shuffleChoiceQuestion(questions[i], answersCorrect[i])
                                break
                            }
                            }
                        }

                        const newData = {
                            testId: testId,
                            user: context.auth.uid,
                            title: testData.title,
                            image: testData.image,
                            pointsMax: testData.pointsMax,
                            timeStarted: Date.now(),
                            timeFinished: Date.now(),
                            isFinished: false,
                            isResultsShown: isResultsShown,
                            isCorrectAnswersAfterQuestionShown: isCorrectAnswersAfterQuestionShown,
                            isNavigationEnabled: isNavigationEnabled,
                            questions: questions,
                            isDemo: isDemo,
                            isGradesEnabled: isGradesEnabled,
                        }

                        if (isGradesEnabled) {
                            newData.grades = grades
                        }

                        if (!isRetakingEnabled && !isDemo) {
                            db.collection('testsPassed')
                                .where('user', '==', context.auth.uid)
                                .where('testId', '==', testId)
                                .where('isDemo', '==', false)
                                .get()
                                .then((snapshot) => {
                                    const docs = snapshot.docs

                                    if (docs.length > 0) {
                                        return reject(new functions.https.HttpsError('permission-denied', 'Test already taken'))
                                    } else {
                                        db.collection("testsPassed").add(newData).then((ref) => {
                                            ref.get().then((testPassed) => {

                                                ref.collection("private").doc("results").set({
                                                    testId: testId,
                                                    answersCorrect: answersCorrect,
                                                    explanations: explanations,
                                                }).then((_1) => {
                                                    return resolve({
                                                        recordId: testPassed.id,
                                                    })
                                                })
                                            })
                                        })
                                    }

                                })
                                .catch((err) => {
                                    console.log('Error getting document', err);
                                    return null;
                                })
                        } else {
                            db.collection("testsPassed").add(newData).then((ref) => {
                                ref.get().then((testPassed) => {

                                    ref.collection("private").doc("results").set({
                                        testId: testId,
                                        answersCorrect: answersCorrect,
                                        explanations: explanations,
                                    }).then((_1) => {
                                        return resolve({
                                            recordId: testPassed.id,
                                        })
                                    })
                                })
                            })
                        }
                    })
                })
            })
        }).catch((err) => {
            console.log('Error occurred', err)
            throw err
        })
    })


exports.finishTest = functions
    .runWith({
        enforceAppCheck: true,
    })
    .https.onCall((data, context) => {

        return new Promise((resolve, reject) => {

            if (context.app == undefined) {
                return reject(new functions.https.HttpsError('failed-precondition', 'App not verified'))
            }

            if (context.auth == null) {
                return reject(new functions.https.HttpsError('unauthenticated', 'Not logged in'))
            }

            const recordId = data.recordId || null
            const questions = JSON.parse(data.questions) || null

            const testRef = db.collection("testsPassed").doc(recordId)

            testRef.get().then((record) => {
                const data = record.data()
                const user = data.user
                const isFinished = data.isFinished
                const isGradesEnabled = data.isGradesEnabled
                const grades = data.grades

                if (user != context.auth.uid) {
                    return reject(new functions.https.HttpsError('permission-denied', 'No access'))
                }
                if (isFinished) {
                    return reject(new functions.https.HttpsError('failed-precondition', 'Test already finished'))
                }

                testRef.collection("private").doc("results").get().then((doc) => {
                    if (!doc.exists) {
                        return reject(new functions.https.HttpsError('not-found', 'Test not found'))
                    }

                    const answersCorrect = doc.data().answersCorrect

                    if (questions.length != answersCorrect.length) {
                        return reject(new functions.https.HttpsError('failed-precondition', 'Incorrect number of questions'))
                    }
                    try {
                        for (let i = 0; i < questions.length; ++i) {
                            if (!validateQuestionFields(questions[i])) throw new Error
                        }
                    } catch (err) {
                        return reject(new functions.https.HttpsError('failed-precondition', 'Invalid data type'))
                    }

                    const unansweredQuestion = validateRequiredQuestions(questions)
                    if (unansweredQuestion != -1) {
                        return reject(new functions.https.HttpsError('failed-precondition', `${unansweredQuestion}: question should be answered`))
                    }

                    const pointsPerQuestion = calculatePoints(questions, answersCorrect)

                    testRef.collection("private").doc("results").update({ pointsPerQuestion: pointsPerQuestion }).then((_1) => {
                        const pointsEarned = pointsPerQuestion.reduce((sum, a) => sum + a, 0)

                        const newData = {
                            questions: questions,
                            pointsEarned: pointsEarned,
                            isFinished: true,
                            timeFinished: Date.now(),
                        }

                        if (isGradesEnabled) {
                            newData.gradeEarned = getGrade(grades, pointsEarned)
                        }

                        testRef.update(newData).then((_1) => {
                            return resolve()
                        })
                    })
                })
            })
        }).catch((err) => {
            console.log('Error occurred', err)
            throw err
        })
    })

exports.calculatePoints = functions
    .runWith({
        enforceAppCheck: true,
    })
    .https.onCall((data, context) => {

        return new Promise((resolve, reject) => {

            if (context.app == undefined) {
                return reject(new functions.https.HttpsError('failed-precondition', 'App not verified'))
            }

            if (context.auth == null) {
                return reject(new functions.https.HttpsError('unauthenticated', 'Not logged in'))
            }

            const recordId = data.recordId || null

            const testRef = db.collection("testsPassed").doc(recordId)

            testRef.get().then((record) => {
                const data = record.data()
                const pointsCalculated = data.pointsCalculated
                const isFinished = data.isFinished
                const questions = data.questions
                const isGradesEnabled = data.isGradesEnabled
                const grades = data.grades

                if (pointsCalculated) {
                    return reject(new functions.https.HttpsError('failed-precondition', 'Points already calculated'))
                }
                if (isFinished) {
                    return reject(new functions.https.HttpsError('failed-precondition', 'Test already finished'))
                }

                testRef.collection("private").doc("results").get().then((doc) => {
                    if (!doc.exists) {
                        return reject(new functions.https.HttpsError('not-found', 'Test not found'))
                    }

                    const answersCorrect = doc.data().answersCorrect

                    if (questions.length != answersCorrect.length) {
                        return reject(new functions.https.HttpsError('failed-precondition', 'Incorrect number of questions'))
                    }
                    try {
                        for (let i = 0; i < questions.length; ++i) {
                            if (!validateQuestionFields(questions[i])) throw new Error
                        }
                    } catch (err) {
                        return reject(new functions.https.HttpsError('failed-precondition', 'Invalid data type'))
                    }

                    const pointsPerQuestion = calculatePoints(questions, answersCorrect)

                    testRef.collection("private").doc("results").update({ pointsPerQuestion: pointsPerQuestion }).then((_1) => {
                        const pointsEarned = pointsPerQuestion.reduce((sum, a) => sum + a, 0)

                        const newData = {
                            questions: questions,
                            pointsEarned: pointsEarned,
                            pointsCalculated: true,
                        }

                        if (isGradesEnabled) {
                            newData.gradeEarned = getGrade(grades, pointsEarned)
                        }

                        testRef.update(newData).then((_1) => {
                            return resolve({ pointsEarned: pointsEarned, gradeEarned: newData.gradeEarned })
                        })
                    })
                })
            })
        }).catch((err) => {
            console.log('Error occurred', err)
            throw err
        })
    })

exports.submitQuestion = functions
    .runWith({
        enforceAppCheck: true,
    })
    .https.onCall((data, context) => {

        return new Promise((resolve, reject) => {

            if (context.app == undefined) {
                return reject(new functions.https.HttpsError('failed-precondition', 'App not verified'))
            }

            if (context.auth == null) {
                return reject(new functions.https.HttpsError('unauthenticated', 'Not logged in'))
            }

            const recordId = data.recordId || null
            const question = JSON.parse(data.question) || null
            const num = data.num

            const testRef = db.collection("testsPassed").doc(recordId)

            testRef.get().then((record) => {
                const testData = record.data()
                const user = testData.user
                const questions = testData.questions
                const isCorrectAnswersAfterQuestionShown = testData.isCorrectAnswersAfterQuestionShown
                const isFinished = testData.isFinished
                const isGradesEnabled = testData.isGradesEnabled
                const grades = testData.grades

                if (user != context.auth.uid) {
                    return reject(new functions.https.HttpsError('permission-denied', 'No access'))
                }
                if (isFinished) {
                    return reject(new functions.https.HttpsError('failed-precondition', 'Test already finished'))
                }

                testRef.collection("private").doc("results").get().then((doc) => {
                    if (!doc.exists) {
                        return reject(new functions.https.HttpsError('not-found', 'Test not found'))
                    }

                    const answersCorrect = doc.data().answersCorrect
                    const explanations = doc.data().explanations
                    const pointsPerQuestion = doc.data().pointsPerQuestion || []

                    if (num != pointsPerQuestion.length) {
                        return reject(new functions.https.HttpsError('failed-precondition', `Incorrect question number`))
                    }

                    if (!validateQuestionFields(question)) {
                        return reject(new functions.https.HttpsError('failed-precondition', 'Invalid data type'))
                    }

                    if (!validateRequiredQuestion(question)) {
                        return reject(new functions.https.HttpsError('failed-precondition', `This question should be answered`))
                    }

                    const points = scoreQuestion(question, answersCorrect[num])
                    pointsPerQuestion.push(points)

                    testRef.collection("private").doc("results").update({ pointsPerQuestion: pointsPerQuestion }).then((_1) => {
                        const pointsEarned = pointsPerQuestion.reduce((sum, a) => sum + a, 0)

                        questions[num] = question

                        const isFinished = num == questions.length - 1

                        const newData = {
                            questions: questions,
                            pointsEarned: pointsEarned,
                            pointsCalculated: true,
                            isFinished: isFinished,
                            timeFinished: Date.now(),
                        }

                        if (isFinished && isGradesEnabled) {
                            newData.gradeEarned = getGrade(grades, pointsEarned)
                        }

                        testRef.update(newData).then((_1) => {
                            const responseData = {
                                points: points,
                            }

                            if (isCorrectAnswersAfterQuestionShown) {
                                responseData.pointsEarned = pointsEarned
                                responseData.answersCorrect = answersCorrect[num]
                                responseData.explanation = explanations[num]
                            }
                            return resolve(responseData)
                        })
                    })
                })
            })
        }).catch((err) => {
            console.log('Error occurred', err)
            throw err
        })
    })


exports.changeResultsShown = functions.firestore
    .document('tests/{test}')
    .onWrite((change, context) => {

        const documentOld = change.before.data()
        const document = change.after.data()
        if (document == undefined || document.isResultsShown == documentOld.isResultsShown) return null

        const testId = change.after.id
        const isResultsShown = document.isResultsShown

        db.collection('testsPassed')
            .where('testId', '==', testId)
            .where('isResultsShown', '!=', isResultsShown)
            .get()
            .then((snapshot) => {
                const docs = snapshot.docs

                for (let i = 0; i < docs.length; ++i) {
                    docs[i].ref.update({ isResultsShown: isResultsShown })
                }
            })
            .catch((err) => {
                console.log('Error getting document', err);
                return null;
            })

        return null
    })


exports.deleteDemoTests = functions.pubsub
    .schedule('0 0 * * 1,4')
    .onRun(async (context) => {
        const tests = db.collection('testsPassed')
        const demoTests = await tests.where('isDemo', '==', true).get()
        demoTests.forEach((doc) => {
            doc.ref.delete()
        })
        return null
    })

function isString(field) {
    return typeof field == "string"
}

function isNumber(field) {
    return typeof field == "number"
}

function isBoolean(field) {
    return typeof field == "boolean"
}

function isStringOrUndefined(field) {
    return field == undefined || typeof field == "string"
}

function isNumberOrUndefined(field) {
    return field == undefined || typeof field == "number"
}

function isBooleanOrUndefined(field) {
    return field == undefined || typeof field == "boolean"
}

function validateQuestionFields(question) {
    for (let i = 0; i < question.answers.length; ++i) {
        if (!validateAnswerFields(question.answers[i])) return false
    }

    return isString(question.id)
        && isString(question.testId)
        && isString(question.title)
        && isString(question.description)
        && isString(question.image)
        && isString(question.type)
        && isBoolean(question.isRequired)
        && isString(question.enteredAnswer)
        && isNumber(question.pointsMax)
        && isNumber(question.pointsEarned)
        && isBoolean(question.isMatch)
        && isBoolean(question.isCaseSensitive)
        && isNumberOrUndefined(question.percentageError)
}

function validateAnswerFields(answer) {
    return isStringOrUndefined(answer.text) && isBooleanOrUndefined(answer.isSelected)
}

function validateGradeFields(grade) {
    return isString(grade.grade) && isNumber(grade.pointsFrom) && isNumber(grade.pointsTo)
}

// returns number of unanswered question or -1 if all required questions answered
function validateRequiredQuestions(questions) {
    for (let i = 0; i < questions.length; ++i) {
        if (!validateRequiredQuestion(questions[i])) return i
    }
    return -1
}

function validateRequiredQuestion(question) {
    if (!question.isRequired) return true
    if (question.enteredAnswer.length > 0) return true
    if (question.type == 'ordering' || question.type == 'matching') return true

    let isSelected = false

    for (let j = 0; j < question.answers.length; ++j) {
        if (question.answers[j].isSelected) {
            isSelected = true
            break
        }
    }

    return isSelected
}

function calculatePoints(questions, answersCorrect) {
    const pointsPerQuestion = []

    for (let i = 0; i < questions.length; ++i) {
        const pointsEarned = scoreQuestion(questions[i], answersCorrect[i])
        pointsPerQuestion.push(pointsEarned)
    }
    return pointsPerQuestion
}

function scoreQuestion(question, answersCorrect) {
    const pointsMax = question.pointsMax
    let pointsEarned = 0

    switch (question.type) {
    case 'short answer': {
        let enteredAnswer = question.enteredAnswer
        const isMatch = question.isMatch
        const isCaseSensitive = question.isCaseSensitive

        if (!isCaseSensitive) enteredAnswer = enteredAnswer.toLowerCase()

        for (let j = 0; j < answersCorrect.answers.length; ++j) {
            let answer = answersCorrect.answers[j].text
            if (!isCaseSensitive) answer = answer.toLowerCase()

            if (isMatch && enteredAnswer == answer || !isMatch && enteredAnswer.includes(answer)) {
                pointsEarned = pointsMax
            }
        }
        break
    }
    case 'matching': {
        let isCorrect = true
        for (let j = 0; j < question.answers.length; ++j) {
            isCorrect = isCorrect && answersCorrect.answers[j].textMatching == question.answers[j].textMatching
        }
        if (isCorrect) pointsEarned = pointsMax
        break
    }
    case 'ordering': {
        let isCorrect = true
        for (let j = 0; j < question.answers.length; ++j) {
            isCorrect = isCorrect && answersCorrect.answers[j].text == question.answers[j].text
        }
        if (isCorrect) pointsEarned = pointsMax
        break
    }
    case 'number': {
        const enteredAnswer = question.enteredAnswer
        const correctNumber = parseFloat(answersCorrect.answers[0].text)
        const percentageError = question.percentageError

        if (percentageError == null) {
            if (enteredAnswer == correctNumber) pointsEarned = pointsMax
        } else {
            const diff = correctNumber * percentageError / 100
            if (enteredAnswer >= correctNumber - diff && enteredAnswer <= correctNumber + diff) {
                pointsEarned = pointsMax
            }
        }
        break
    }
    default: {
        let isCorrect = true
        for (let j = 0; j < question.answers.length; ++j) {
            isCorrect = isCorrect && answersCorrect.answers[j].isCorrect == question.answers[j].isSelected
        }
        if (isCorrect) pointsEarned = pointsMax
    }
    }

    return pointsEarned
}

function getGrade(grades, pointsEarned) {
    for (let i = 0; i < grades.length; ++i) {
        if (grades[i].pointsFrom <= pointsEarned && grades[i].pointsTo >= pointsEarned) {
            return grades[i].grade
        }
    }
    return ''
}

function shuffleMatchingQuestion(question) {
    const textMatchingArray = []
    for (let j = 0; j < question.answers.length; ++j) {
        textMatchingArray.push(question.answers[j].textMatching)
    }
    shuffleArray(textMatchingArray)
    for (let j = 0; j < question.answers.length; ++j) {
        question.answers[j].textMatching = textMatchingArray[j]
    }
}

function shuffleOrderingQuestion(question) {
    shuffleArray(question.answers)
}

function shuffleChoiceQuestion(question, answersCorrect) {
    shuffle(question.answers, answersCorrect.answers)
}

function shuffleArray(array) {
    for (let i = array.length - 1; i > 0; --i) {
        const j = Math.floor(Math.random() * (i + 1))
        const temp = array[i]
        array[i] = array[j]
        array[j] = temp
    }
}

function shuffle(...args) {
    let arrLength = 0
    const argsLength = arguments.length
    let rnd
    let tmp

    for (let index = 0; index < argsLength; index += 1) {
        if (index === 0) {
            arrLength = args[0].length
        }

        if (arrLength !== args[index].length) {
            throw new RangeError("Array lengths do not match")
        }
    }

    while (arrLength) {
        rnd = Math.floor(Math.random() * arrLength)
        arrLength -= 1

        for (let argsIndex = 0; argsIndex < argsLength; argsIndex += 1) {
            tmp = args[argsIndex][arrLength]
            args[argsIndex][arrLength] = args[argsIndex][rnd]
            args[argsIndex][rnd] = tmp
        }
    }
}
