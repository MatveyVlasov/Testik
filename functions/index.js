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

exports.addQuestionsToTestPassed = functions.firestore
    .document('testsPassed/{test}')
    .onCreate(async (snap, context) => {
        const data = snap.data()

        const test = await db.collection("tests").doc(data.testId).collection("private").doc("questions").get()
        const questions = test.data().questions
        const newQuestions = []

        for (let i = 0; i < questions.length; ++i) {
            const q = questions[i]
            q.answers = q.answers.map((ans) => ({
                ...ans,
                isCorrect: false,
            }))
            newQuestions.push(questions[i])
        }

        return snap.ref.set({
            questions: newQuestions,
        }, { merge: true })
    })
