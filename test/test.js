const firebase = require('@firebase/testing')

const MY_PROJECT_ID = "tests-android-app"
const myId = "user_abc"
const theirId = "user_xyz"
const myEmail = "user_abc@gmail.com"
const theirEmail = "user_xyz@gmail.com"
const myAuth = { uid: myId, email: myEmail }

function getFirestore(auth = null) {
    return firebase.initializeTestApp({ projectId: MY_PROJECT_ID, auth: auth }).firestore()
}

function getAdminFirestore() {
    return firebase.initializeAdminApp({ projectId: MY_PROJECT_ID }).firestore()
}

beforeEach(async() => {
    await firebase.clearFirestoreData({ projectId: MY_PROJECT_ID })
})

describe("Users collection", () => {

    const COLLECTION = "users"

    it("Can read another user info when logged in", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(theirId)
        await firebase.assertSucceeds(doc.get())
    })

    it("Can't read user info when not logged in", async () => {
        const db = getFirestore()
        const doc = db.collection(COLLECTION).doc(theirId)
        await firebase.assertFails(doc.get())
    })

    it("Can create user with my id", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertSucceeds(doc.set({ email: myEmail, username: "username" }))
    })

    it("Can't create user with their id", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(theirId)
        await firebase.assertFails(doc.set({ email: myEmail, username: "username" }))
    })

    it("Can't create user with their email", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertFails(doc.set({ email: theirEmail, username: "username" }))
    })

    it("Can't create user when not logged in", async () => {
        const db = getFirestore()
        const doc = db.collection(COLLECTION).doc(theirId)
        await firebase.assertFails(doc.set({ email: theirEmail, username: "username" }))
    })

    it("Can't create user without email", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertFails(doc.set({ username: "username" }))
    })

    it("Can't create user without username", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertFails(doc.set({ email: myEmail }))
    })

    it("Can't create user with incorrect username type", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertFails(doc.set({ email: myEmail, username: 123 }))
    })

    it("Can't create user with too long username", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertFails(doc.set({ email: myEmail, username: "123".repeat(100) }))
    })

    it("Can't create user with too short username", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertFails(doc.set({ email: myEmail, username: "12" }))
    })

    it("Can update user with my id", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc(myId)
        await setupDoc.set({ email: myEmail, username: "username" })

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertSucceeds(doc.update({ username: "usernamenew" }))
    })

    it("Can't update user with their id", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc(theirId)
        await setupDoc.set({ email: theirEmail, username: "username" })

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(theirId)
        await firebase.assertFails(doc.update({ username: "usernameNew" }))
    })

    it("Can't update email", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc(myId)
        await setupDoc.set({ email: myEmail, username: "username" })

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertFails(doc.set({ email: "newEmail" }))
    })

    it("Can't delete user", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc(myId)
        await setupDoc.set({ email: myEmail, username: "username" })

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertFails(doc.delete())
    })

    it("Can't delete user with their id", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc(theirId)
        await setupDoc.set({ email: theirEmail, username: "username"})

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(theirId)
        await firebase.assertFails(doc.delete())
    })
})

describe("Tests collection", () => {

    const COLLECTION = "tests"
    const MY_TEST = { author: myId, title: "title", category: "category", lastUpdated: Date.now() - 10000}
    const THEIR_TEST = { author: theirId, title: "title", category: "category", lastUpdated: Date.now() - 10000}

    it("Can read their test info when logged in", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(theirId)
        await firebase.assertSucceeds(doc.get())
    })

    it("Can read test info when not logged in", async () => {
        const db = getFirestore()
        const doc = db.collection(COLLECTION).doc(theirId)
        await firebase.assertSucceeds(doc.get())
    })

    it("Can't read multiple tests info when not logged in", async () => {
        const db = getFirestore()
        const doc = db.collection(COLLECTION)
        await firebase.assertFails(doc.get())
    })

    it("Can create test with my id", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertSucceeds(doc.set({ author: myId, title: "title", category: "category", lastUpdated: Date.now() }))
    })

    it("Can't create test with their id", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertFails(doc.set({ author: theirId, title: "title", category: "category", lastUpdated: Date.now() }))
    })

    it("Can't create test when not logged in", async () => {
        const db = getFirestore()
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertFails(doc.set({ author: theirId, title: "title", category: "category", lastUpdated: Date.now() }))
    })

    it("Can't create test without author", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertFails(doc.set({ title: "title", category: "category", lastUpdated: Date.now() }))
    })

    it("Can't create test without title", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertFails(doc.set({ author: myId, category: "category", lastUpdated: Date.now() }))
    })

    it("Can't create test with incorrect time", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertFails(doc.set({ author: myId, title: "title", category: "category", lastUpdated: Date.now() - 10000 }))
    })

    it("Can't create test with incorrect title type", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertFails(doc.set({ author: myId, title: 123, category: "category", lastUpdated: Date.now() }))
    })

    it("Can't create test with too long title", async () => {
        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertFails(doc.set({ author: myId, title: "123".repeat(100), category: "category", lastUpdated: Date.now() }))
    })

    it("Can update my test", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("testId")
        await setupDoc.set(MY_TEST)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertSucceeds(doc.update({ title: "newtitle", lastUpdated: Date.now() }))
    })

    it("Can't update their test", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("testId")
        await setupDoc.set(THEIR_TEST)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertFails(doc.update({ title: "newtitle", lastUpdated: Date.now() }))
    })

    it("Can't update test author", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("testId")
        await setupDoc.set(MY_TEST)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertFails(doc.update({ author: theirId, lastUpdated: Date.now() }))
    })

    it("Can't update test without timestamp", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("testId")
        await setupDoc.set(MY_TEST)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertFails(doc.update({ title: "newtitle" }))
    })

    it("Can delete my test", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("testId")
        await setupDoc.set(MY_TEST)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertSucceeds(doc.delete())
    })

    it("Can't delete their test", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("testId")
        await setupDoc.set(THEIR_TEST)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId")
        await firebase.assertFails(doc.delete())
    })

    it("Can create test questions with my id", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("testId")
        await setupDoc.set(MY_TEST)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId").collection("private").doc("questions")
        await firebase.assertSucceeds(doc.set({ answersCorrect: "answers", questions: "questions" }))
    })

    it("Can't create test questions with their id", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("testId")
        await setupDoc.set(THEIR_TEST)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId").collection("private").doc("questions")
        await firebase.assertFails(doc.set({ answersCorrect: "answers", questions: "questions" }))
    })

    it("Can delete my test questions", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("testId")
        await setupDoc.set(MY_TEST)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId").collection("private").doc("questions")
        await firebase.assertSucceeds(doc.delete())
    })

    it("Can't delete their test questions", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("testId")
        await setupDoc.set(THEIR_TEST)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("testId").collection("private").doc("questions")
        await firebase.assertFails(doc.delete())
    })
})

describe("Tests passed collection", () => {

    const COLLECTION = "testsPassed"
    const MY_RECORD = { user: myId, testId: "testId", title: "title", questions: "questions",
            timeStarted: Date.now() - 15000, timeFinished: Date.now() - 10000, isFinished: false, pointsMax: 5 }
    const THEIR_RECORD = { user: theirId, testId: "testId", title: "title", questions: "questions",
    timeStarted: Date.now() - 15000, timeFinished: Date.now() - 10000, isFinished: false, pointsMax: 5 }

    it("Can read my test record", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("recordId")
        await setupDoc.set(MY_RECORD)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId")
        await firebase.assertSucceeds(doc.get())
    })

    it("Can't read their test record", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("recordId")
        await setupDoc.set(THEIR_RECORD)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId")
        await firebase.assertFails(doc.get())
    })

    it("Can read test record of my test", async () => {
        const admin = getAdminFirestore()

        const setupDoc = admin.collection("tests").doc("testId")
        await setupDoc.set({ author: myId, title: "title", category: "category", lastUpdated: Date.now() - 10000})

        const setupDoc2 = admin.collection(COLLECTION).doc("recordId")
        await setupDoc2.set(THEIR_RECORD)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId")
        await firebase.assertSucceeds(doc.get())
    })

    it("Can update my test record", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("recordId")
        await setupDoc.set(MY_RECORD)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId")
        await firebase.assertSucceeds(doc.update({ questions: "updatedQuestions", timeFinished: Date.now() }))
    })

    it("Can't update their test record", async () => {
        const admin = getAdminFirestore()

        const setupDoc = admin.collection("tests").doc("testId")
        await setupDoc.set({ author: myId, title: "title", category: "category", lastUpdated: Date.now() - 10000})

        const setupDoc2 = admin.collection(COLLECTION).doc("recordId")
        await setupDoc2.set(THEIR_RECORD)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId")
        await firebase.assertFails(doc.update({ questions: "updatedQuestions", timeFinished: Date.now() }))
    })

    it("Can't update test record without timestamp", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("recordId")
        await setupDoc.set(MY_RECORD)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId")
        await firebase.assertFails(doc.update({ questions: "updatedQuestions" }))
    })

    it("Can't update test record with incorrect timestamp", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("recordId")
        await setupDoc.set(MY_RECORD)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId")
        await firebase.assertFails(doc.update({ questions: "updatedQuestions", timeFinished: Date.now() - 10000 }))
    })

    it("Can't change test record testId", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("recordId")
        await setupDoc.set(MY_RECORD)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId")
        await firebase.assertFails(doc.update({ testId: "newTestId", timeFinished: Date.now() }))
    })

    it("Can't delete test record", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("recordId")
        await setupDoc.set(MY_RECORD)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId")
        await firebase.assertFails(doc.delete())
    })

    it("Can't read my test record results", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("recordId")
        await setupDoc.set(MY_RECORD)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId").collection("private").doc("results")
        await firebase.assertFails(doc.get())
    })

    it("Can read results of my test", async () => {
        const admin = getAdminFirestore()

        const setupDoc = admin.collection("tests").doc("testId")
        await setupDoc.set({ author: myId, title: "title", category: "category", lastUpdated: Date.now() - 10000})

        const setupDoc2 = admin.collection(COLLECTION).doc("recordId")
        await setupDoc2.set({ user: theirId, testId: "testId", title: "title", questions: "questions",
        timeStarted: Date.now() - 15000, timeFinished: Date.now() - 10000, isFinished: false, pointsMax: 5 })

        const setupDoc3 = admin.collection(COLLECTION).doc("recordId").collection("private").doc("results")
        await setupDoc3.set({ testId: "testId", answersCorrect: "answers" })

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId").collection("private").doc("results")
        await firebase.assertSucceeds(doc.get())
    })

    it("Can't edit my test results", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("recordId")
        await setupDoc.set(MY_RECORD)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId").collection("private").doc("results")
        await firebase.assertFails(doc.set({}))
    })

    it("Can't delete my test results", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc("recordId")
        await setupDoc.set(MY_RECORD)

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc("recordId").collection("private").doc("results")
        await firebase.assertFails(doc.delete())
    })
})