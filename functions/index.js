const functions = require("firebase-functions")

const admin = require('firebase-admin')
admin.initializeApp()

const db = admin.firestore()

// exports.calculateMaxPoints = functions.firestore
//     .document('tests/{test}')
//     .onWrite((change, context) => {
//         const document = change.after.data()
//         console.log("Look Start")
//         if (document == undefined || document.pointsUpdated) return null

//         const questions = document.questions
//         if (questions == undefined) return null
//         let pointsMax = 0
//         for (let i = 0; i < questions.length; ++i) {
//             pointsMax += questions[i].pointsMax
//         }

//         const data1 = change.after.ref.collection('private')
//         const data2 = data1.doc('questions').get().then((doc) => {
//             if (doc.exists) {
//                 const data3 = doc.data()
//                 if (data3 != undefined) {
//                     const data4 = data3.questions
//                     console.log(`${JSON.stringify(data4)}`)
//                 }
//             }
//         }).catch((err) => {
//             console.log('Error getting document', err);
//             return null;
//         })


//         return change.after.ref.set({
//             pointsMax: pointsMax,
//             pointsUpdated: true,
//         }, { merge: true })
//     })

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

            const testRef = db.collection("tests").doc(testId)


            testRef.get().then((doc) => {
                if (!doc.exists) {
                    return reject(new functions.https.HttpsError('not-found', 'Test not found'))
                }

                const testData = doc.data()
                const isGradesEnabled = testData.isGradesEnabled || false
                const grades = testData.grades || []

                if (isDemo && testData.author != context.auth.uid) {
                    return reject(new functions.https.HttpsError('permission-denied', 'No access'))
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
                        questions: questions,
                        isDemo: isDemo,
                        isGradesEnabled: isGradesEnabled,
                    }

                    if (isGradesEnabled) {
                        newData.grades = grades
                    }

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
        && isNumber(question.correctNumber)
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
        if (questions[i].enteredAnswer.length > 0) continue

        let isSelected = false

        for (let j = 0; j < questions[i].answers.length; ++j) {
            if (questions[i].answers[j].isSelected) {
                isSelected = true
                break
            }
        }

        if (questions[i].isRequired && !isSelected) return i
    }
    return -1
}

function calculatePoints(questions, answersCorrect) {
    const pointsPerQuestion = []

    for (let i = 0; i < questions.length; ++i) {
        const pointsMax = questions[i].pointsMax
        let pointsEarned = 0

        switch (questions[i].type) {
        case 'short answer': {
            let enteredAnswer = questions[i].enteredAnswer
            const isMatch = questions[i].isMatch
            const isCaseSensitive = questions[i].isCaseSensitive

            if (!isCaseSensitive) enteredAnswer = enteredAnswer.toLowerCase()

            for (let j = 0; j < answersCorrect[i].answers.length; ++j) {
                let answer = answersCorrect[i].answers[j].text
                if (!isCaseSensitive) answer = answer.toLowerCase()

                if (isMatch && enteredAnswer == answer || !isMatch && enteredAnswer.includes(answer)) {
                    pointsEarned = pointsMax
                }
            }
            break
        }
        case 'matching': {
            let isCorrect = true
            for (let j = 0; j < questions[i].answers.length; ++j) {
                isCorrect = isCorrect && answersCorrect[i].answers[j].textMatching == questions[i].answers[j].textMatching
            }
            if (isCorrect) pointsEarned = pointsMax
            break
        }
        case 'ordering': {
            let isCorrect = true
            for (let j = 0; j < questions[i].answers.length; ++j) {
                isCorrect = isCorrect && answersCorrect[i].answers[j].text == questions[i].answers[j].text
            }
            if (isCorrect) pointsEarned = pointsMax
            break
        }
        case 'number': {
            const enteredAnswer = questions[i].enteredAnswer
            const correctNumber = questions[i].correctNumber
            const percentageError = questions[i].percentageError

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
            for (let j = 0; j < questions[i].answers.length; ++j) {
                isCorrect = isCorrect && answersCorrect[i].answers[j].isCorrect == questions[i].answers[j].isSelected
            }
            if (isCorrect) pointsEarned = pointsMax
        }
        }

        pointsPerQuestion.push(pointsEarned)
    }
    return pointsPerQuestion
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

function shuffleArray(array) {
    for (let i = array.length - 1; i > 0; --i) {
        const j = Math.floor(Math.random() * (i + 1))
        const temp = array[i]
        array[i] = array[j]
        array[j] = temp
    }
}
