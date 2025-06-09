package com.cipherxzc.whatsnext.data.repository

import com.cipherxzc.whatsnext.data.database.AppDatabase
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.data.database.TodoItemDao
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TodoRepository(
    private val database: AppDatabase
) {
    private val todoItemDao: TodoItemDao = database.todoItemDao()

    private fun generateDocumentId(): String {
        return FirebaseFirestore.getInstance()
            .collection("anything")
            .document().id
    }

    suspend fun getItemById(id: String): TodoItem? = todoItemDao.getItemById(id)
    suspend fun getItemsByUser(userId: String): List<TodoItem> = todoItemDao.getItemsByUser(userId)
    suspend fun getUnSyncedItems(userId: String): List<TodoItem> = todoItemDao.getUnSyncedItems(userId)

    private suspend fun insertOrUpdateItem(item: TodoItem) = todoItemDao.insertOrUpdate(item)

    suspend fun insertItem(
        userId: String,
        title: String,
        detail: String = "",
        dueDate: Timestamp? = null,
        importance: Int? = null
    ): TodoItem {
        val item = TodoItem(
            id = generateDocumentId(),
            userId = userId,
            title = title,
            detail = detail,
            dueDate = dueDate,
            importance = importance
        )
        insertOrUpdateItem(item)
        return item
    }

    private suspend fun modifyItem(
        id: String,
        title: String? = null,
        detail: String? = null,
        dueDate: Timestamp? = null,
        importance: Int? = null,
        isCompleted: Boolean? = null,
        isDeleted: Boolean? = null
    ) {
        val item = getItemById(id)
        item?.let {
            val updatedItem = it.copy(
                title = title ?: it.title,
                detail = detail ?: it.detail,
                dueDate = dueDate ?: it.dueDate,
                importance = importance ?: it.importance,
                isCompleted = isCompleted ?: it.isCompleted,
                isDeleted = isDeleted ?: it.isDeleted,
                // 同步信息
                lastModified = Timestamp.now(),
                isSynced = false
            )
            insertOrUpdateItem(updatedItem)
        }
    }

    suspend fun updateItem(
        id: String,
        title: String? = null,
        detail: String? = null,
        dueDate: Timestamp? = null,
        isCompleted: Boolean? = null,
        importance: Int? = null
    ) = modifyItem(id, title, detail, dueDate, importance, isCompleted)

    suspend fun completeItem(id: String) = modifyItem(id, isCompleted = true)
    suspend fun unCompleteItem(id: String) = modifyItem(id, isCompleted = false)
    suspend fun deleteItem(id: String) = modifyItem(id, isDeleted = true)

    suspend fun removeItem(id: String) = withContext(Dispatchers.IO){
        todoItemDao.deleteById(id)
    }

    suspend fun upsertItem(item: TodoItem) {
        if (item.isDeleted) {
            // delete 具有最高的优先级，即使不是最新的，任何客户端delete了其他地方都不该保留
            removeItem(item.id)
        } else {
            val databaseItem = getItemById(item.id)
            if (databaseItem == null || databaseItem.lastModified < item.lastModified){
                insertOrUpdateItem(item.copy(
                    isSynced = true
                ))
            }
        }
    }

    suspend fun insertDefaultData(userId: String) {
        insertItem(
            userId = userId,
            title = "学习如何使用 What's Next",
            detail = "这是一个默认的待办事项，您可以删除或修改它。"
        )
        insertItem(
            userId = userId,
            title = "添加您的第一个 Todo",
            detail = "点击顶部的加号按钮，开始添加您的第一个待办事项！"
        )
    }
}