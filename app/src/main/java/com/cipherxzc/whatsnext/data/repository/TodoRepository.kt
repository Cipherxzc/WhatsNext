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
    suspend fun getUnsyncedItems(userId: String): List<TodoItem> = todoItemDao.getUnsyncedItems(userId)

    private suspend fun insertOrUpdateItem(item: TodoItem) = todoItemDao.insertOrUpdate(item)

    suspend fun insertItem(
        userId: String,
        title: String,
        description: String? = null,
        dueDate: Timestamp? = null
    ): TodoItem {
        val item = TodoItem(
            id = generateDocumentId(),
            userId = userId,
            title = title,
            description = description,
            dueDate = dueDate
        )
        insertOrUpdateItem(item)
        return item
    }

    private fun TodoItem.modify(
        title: String = this.title,
        description: String? = this.description,
        dueDate: Timestamp? = this.dueDate,
        isCompleted: Boolean = this.isCompleted,
        isDeleted: Boolean = this.isDeleted
    ): TodoItem {
        return this.copy(
            title = title,
            description = description,
            dueDate = dueDate,
            isCompleted = isCompleted,
            isDeleted = isDeleted,
            // 同步信息
            lastModified = Timestamp.now(),
            isSynced = false
        )
    }

    suspend fun modifyItem(
        id: String,
        title: String? = null,
        description: String? = null,
        dueDate: Timestamp? = null,
        isCompleted: Boolean? = null,
        isDeleted: Boolean? = null
    ) {
        val item = getItemById(id)
        item?.let {
            val updatedItem = it.modify(
                title = title ?: it.title,
                description = description ?: it.description,
                dueDate = dueDate ?: it.dueDate,
                isCompleted = isCompleted ?: it.isCompleted,
                isDeleted = isDeleted ?: it.isDeleted
            )
            insertOrUpdateItem(updatedItem)
        }
    }

    suspend fun completeItem(id: String) = modifyItem(id, isCompleted = true)
    suspend fun uncompleteItem(id: String) = modifyItem(id, isCompleted = false)
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
            description = "这是一个默认的待办事项，您可以删除或修改它。"
        )
        insertItem(
            userId = userId,
            title = "添加您的第一个 Todo",
            description = "点击右下角的加号按钮，开始添加您的第一个待办事项！"
        )
    }
}