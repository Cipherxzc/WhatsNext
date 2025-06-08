package com.cipherxzc.whatsnext.data.repository

import com.cipherxzc.whatsnext.data.database.TodoItem
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

// Data Transfer Object for Firestore
@IgnoreExtraProperties
private data class FirestoreTodoItemDto(
    var title: String = "",
    var detail: String = "",
    var dueDate: Timestamp? = null,
    var isCompleted: Boolean = false,
    // extra info
    var lastModified: Timestamp = Timestamp.now(),
    var isDeleted: Boolean = false
)

// 封装 Firestore 同步逻辑
// structure:
// /users/{userId}/todo_items/{itemId}
// /users/{userId}/clock-in_items/{itemId}/clock-in_records/{recordId}
class CloudRepository(
    private val firestore: FirebaseFirestore
) {
    private fun TodoItem.toDto(): FirestoreTodoItemDto = FirestoreTodoItemDto(
        title = title,
        detail = detail,
        dueDate = dueDate,
        isCompleted = isCompleted,
        lastModified = lastModified,
        isDeleted = isDeleted
    )

    private fun FirestoreTodoItemDto.toEntity(itemId: String, userId: String): TodoItem = TodoItem(
        id = itemId,
        userId = userId,
        title = title,
        detail = detail,
        dueDate = dueDate,
        isCompleted = isCompleted,
        lastModified = lastModified,
        isSynced = true,  // 已与云端同步
        isDeleted = isDeleted
    )

    suspend fun pushItems(userId: String, items: List<TodoItem>) {
        if (items.isEmpty()) return

        val batch = firestore.batch()
        val itemCollection = firestore.collection("users")
            .document(userId)
            .collection("todo_items")
        items.forEach { item ->
            val itemDoc = itemCollection.document(item.id)
            batch.set(itemDoc, item.toDto(), SetOptions.merge())
        }
        batch.commit().await()
    }

    suspend fun fetchUpdatedItems(userId: String, since: Timestamp): List<TodoItem> {
        val snapshot = firestore
            .collection("users")
            .document(userId)
            .collection("todo_items")
            .whereGreaterThan("lastModified", since)
            .get()
            .await()

        return snapshot.documents.map { doc ->
            val dto = doc.toObject(FirestoreTodoItemDto::class.java) ?: return@map null
            dto.toEntity(itemId = doc.id, userId = userId)
        }.filterNotNull()
    }
}