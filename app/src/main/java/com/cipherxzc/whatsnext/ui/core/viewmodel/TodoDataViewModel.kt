package com.cipherxzc.whatsnext.ui.core.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.cipherxzc.whatsnext.data.database.AppDatabase
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.data.repository.TodoRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TodoDataViewModel(application: Application) : AndroidViewModel(application) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            "whats_next_database"
        ).build()
    }

    private val todoRepo: TodoRepository by lazy {
        TodoRepository(database)
    }

    private var currentUserId: String? = null

    fun setCurrentUser(userId: String) {
        currentUserId = userId
    }

    fun resetCurrentUser() {
        currentUserId = null
    }

    fun getCurrentUser(): String {
        return currentUserId?: throw IllegalStateException("Current user ID is not set")
    }

    suspend fun insertItem(
        title: String,
        detail: String = "",
        dueDate: Timestamp? = null,
        userId: String? = currentUserId
    ): TodoItem {
        if (userId == null) {
            throw IllegalStateException("Current user ID is not set")
        }
        return todoRepo.insertItem(userId, title, detail, dueDate)
    }

    fun insertDefaultData(onComplete: (() -> Unit)? = null, userId: String? = currentUserId) {
        if (userId == null) {
            throw IllegalStateException("Current user ID is not set")
        }
        viewModelScope.launch(Dispatchers.IO) {
            todoRepo.insertDefaultData(userId)

            onComplete?.let {
                withContext(Dispatchers.Main) {
                    it()
                }
            }
        }
    }

    fun updateItem(
        id: String,
        title: String? = null,
        detail: String? = null,
        dueDate: Timestamp? = null,
        isCompleted: Boolean? = null,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            todoRepo.updateItem(
                id = id,
                title = title,
                detail = detail,
                dueDate = dueDate,
                isCompleted = isCompleted,
            )
        }
    }

    suspend fun deleteItem(itemId: String) = todoRepo.deleteItem(itemId)

    suspend fun completeItem(itemId: String) = todoRepo.completeItem(itemId)
    suspend fun unCompleteItem(itemId: String) = todoRepo.unCompleteItem(itemId)

    suspend fun getItem(itemId: String): TodoItem? {
        return todoRepo.getItemById(itemId)
    }

    suspend fun getAllItems(userId: String? = currentUserId): List<TodoItem> {
        if (userId == null) {
            throw IllegalStateException("Current user ID is not set")
        }
        return todoRepo.getItemsByUser(userId)
    }

    suspend fun upsertItems(todoItems: List<TodoItem>) {
        todoItems.forEach {
            todoRepo.upsertItem(it)
        }
    }

    suspend fun getUnsyncedItems(userId: String? = currentUserId): List<TodoItem> {
        if (userId == null) {
            throw IllegalStateException("Current user ID is not set")
        }
        return todoRepo.getUnsyncedItems(userId)
    }
}