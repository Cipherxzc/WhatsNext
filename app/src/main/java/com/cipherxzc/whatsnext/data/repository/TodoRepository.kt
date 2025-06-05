package com.cipherxzc.whatsnext.data.repository

import com.cipherxzc.whatsnext.data.database.AppDatabase
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.data.database.TodoItemDao
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId

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

    private suspend fun insertOrUpdateItem(item: TodoItem) {
        todoItemDao.insertOrUpdate(item)
    }

    suspend fun insertItem(userId: String, title: String, description: String?, dueDate: Timestamp?): TodoItem {
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

    suspend fun upsertItem(item: TodoItem) {
        if (item.isDeleted) {
            // delete 具有最高的优先级，即使不是最新的，任何客户端delete了其他地方都不该保留
            deleteItem(item.itemId)
        } else {
            val databaseItem = getItemById(item.itemId)
            if (databaseItem == null || databaseItem.lastModified <= item.lastModified){
                insertOrUpdateItem(item.copy(
                    clockInCount = databaseItem?.clockInCount ?: 0,
                    isSynced = true
                ))
            }
        }
    }

    suspend fun insertDefaultData(userId: String) = withContext(Dispatchers.IO) {
        val defaultItems = listOf(
            ClockInItem(
                itemId    = generateDocumentId(),
                userId    = userId,
                name      = "早起",
                description  = "早睡早起身体好！"
            ),
            ClockInItem(
                itemId    = generateDocumentId(),
                userId    = userId,
                name      = "锻炼",
                description  = "无体育，不华清！"
            ),
            ClockInItem(
                itemId    = generateDocumentId(),
                userId    = userId,
                name      = "读书",
                description  = "书山有路勤为径！"
            ),
            ClockInItem(
                itemId    = generateDocumentId(),
                userId    = userId,
                name      = "背单词",
                description  = "目标托福105分！"
            )
        )

        defaultItems.forEach { insertOrUpdateItem(it) }

        val defaultRecords = listOf(
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 1, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 7, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 8, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 9, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 10, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 11, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 12, 9, 0),
            defaultItems[0].itemId to LocalDateTime.of(2025, Month.APRIL, 13, 9, 0),
            defaultItems[1].itemId to LocalDateTime.of(2025, Month.APRIL, 10, 9, 0),
            defaultItems[2].itemId to LocalDateTime.of(2025, Month.APRIL, 4, 9, 0),
            defaultItems[2].itemId to LocalDateTime.of(2025, Month.APRIL, 10, 9, 0),
            defaultItems[2].itemId to LocalDateTime.of(2025, Month.APRIL, 13, 9, 0)
        )

        val converter: (LocalDateTime) -> Timestamp = { localDateTime ->
            val zoneId = ZoneId.systemDefault()
            val instant = localDateTime.atZone(zoneId).toInstant()
            Timestamp(instant.epochSecond, instant.nano)
        }

        defaultRecords.forEach { (itemId, localDateTime) ->
            val record = ClockInRecord(
                recordId  = generateDocumentId(),
                userId    = userId,
                itemId    = itemId,
                timestamp = converter(localDateTime),
            )
            insertOrUpdateRecord(record)
        }
    }
}