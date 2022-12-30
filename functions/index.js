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


exports.startTest = functions.https.onCall((data, context) => {

    return new Promise((resolve, reject) => {

        if (context.auth == null) {
            reject(new functions.https.HttpsError('unauthenticated', 'Not logged in'))
        }

        const testId = data.testId || null

        const testRef = db.collection("tests").doc(testId)


        testRef.get().then((doc) => {
            if (doc.exists) {
                const testData = doc.data()

                testRef.collection("private").doc("questions").get().then((docQuestions) => {
                    const questions = docQuestions.data().questions
                    const answersCorrect = docQuestions.data().answersCorrect

                    if (questions.length == 0) {
                        reject(new functions.https.HttpsError('failed-precondition', 'No questions'))
                    }
                    try {
                        for (let i = 0; i < questions.length; ++i) {
                            if (!validateQuestionFields(questions[i])) throw new Error
                        }
                    } catch (err) {
                        reject(new functions.https.HttpsError('failed-precondition', 'Invalid data type'))
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
                    }

                    db.collection("testsPassed").add(newData).then((ref) => {
                        ref.get().then((testPassed) => {

                            ref.collection("private").doc("results").set({ testId: testId, answersCorrect: answersCorrect }).then((_1) => {
                                resolve({
                                    recordId: testPassed.id,
                                })
                            })
                        })
                    })
                })
            } else {
                reject(new functions.https.HttpsError('not-found', 'Test not found'))
            }
        })
    }).catch((err) => {
        console.log('Error occurred', err);
        throw err;
    })
})


exports.finishTest = functions.https.onCall((data, context) => {

    return new Promise((resolve, reject) => {

        if (context.auth == null) {
            reject(new functions.https.HttpsError('unauthenticated', 'Not logged in'))
        }

        const recordId = data.recordId || null
        const questions = JSON.parse(data.questions) || null

        const testRef = db.collection("testsPassed").doc(recordId)

        testRef.get().then((record) => {
            const user = record.data().user
            const isFinished = record.data().isFinished

            if (user != context.auth.uid) {
                reject(new functions.https.HttpsError('permission-denied', 'No access'))
            }
            if (isFinished) {
                reject(new functions.https.HttpsError('failed-precondition', 'Test already finished'))
            }

            testRef.collection("private").doc("results").get().then((doc) => {
                if (doc.exists) {
                    const answersCorrect = doc.data().answersCorrect
                    const pointsPerQuestion = []

                    if (questions.length != answersCorrect.length) {
                        reject(new functions.https.HttpsError('failed-precondition', 'Incorrect number of questions'))
                    }
                    try {
                        for (let i = 0; i < questions.length; ++i) {
                            if (!validateQuestionFields(questions[i])) throw new Error
                        }
                    } catch (err) {
                        reject(new functions.https.HttpsError('failed-precondition', 'Invalid data type'))
                    }

                    for (let i = 0; i < questions.length; ++i) {
                        let isCorrect = true
                        for (let j = 0; j < questions[i].answers.length; ++j) {
                            isCorrect = isCorrect && answersCorrect[i].answers[j].isCorrect == questions[i].answers[j].isSelected
                        }
                        pointsPerQuestion.push(isCorrect? questions[i].pointsMax : 0)
                    }

                    testRef.collection("private").doc("results").update({ pointsPerQuestion: pointsPerQuestion }).then((_1) => {
                        const pointsEarned = pointsPerQuestion.reduce((sum, a) => sum + a, 0)

                        const newData = {
                            questions: questions,
                            pointsEarned: pointsEarned,
                            isFinished: true,
                            timeFinished: Date.now(),
                        }
                        testRef.update(newData).then((_1) => {
                            resolve()
                        })
                    })
                } else {
                    reject(new functions.https.HttpsError('not-found', 'Test not found'))
                }
            })
        })
    }).catch((err) => {
        console.log('Error occurred', err);
        throw err;
    })
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
        && isString(question.enteredAnswer)
        && isNumber(question.pointsMax)
        && isNumber(question.pointsEarned)
}

function validateAnswerFields(answer) {
    return isString(answer.text) && isBoolean(answer.isSelected)
}
