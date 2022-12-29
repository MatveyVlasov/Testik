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

    it("Can't read another user info when not logged in", async () => {
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
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertFails(doc.set({ email: myEmail, username: "username" }))
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

    it("Can update user with my id", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc(myId)
        await setupDoc.set({ email: myEmail, username: "username"})

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertSucceeds(doc.update({ username: "usernamenew" }))
    })

    it("Can't update user with their id", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc(theirId)
        await setupDoc.set({ email: theirEmail, username: "username"})

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(theirId)
        await firebase.assertFails(doc.update({ username: "usernameNew" }))
    })

    it("Can't update email", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc(myId)
        await setupDoc.set({ email: myEmail, username: "username"})

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertFails(doc.set({ email: "newEmail" }))
    })

    it("Can delete user with my id", async () => {
        const admin = getAdminFirestore()
        const setupDoc = admin.collection(COLLECTION).doc(myId)
        await setupDoc.set({ email: myEmail, username: "username"})

        const db = getFirestore(myAuth)
        const doc = db.collection(COLLECTION).doc(myId)
        await firebase.assertSucceeds(doc.delete())
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