package com.mostgymapp.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getWebViewUrl(): String? {
        return try {
            val document = firestore.collection("config").document("app").get().await()
            document?.getString("url")?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            null
        }
    }
}
