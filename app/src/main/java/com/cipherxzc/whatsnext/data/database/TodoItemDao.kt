package com.cipherxzc.whatsnext.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TodoItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: TodoItem)

    @Delete
    suspend fun delete(item: TodoItem)

    @Query("DELETE FROM todo_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM todo_items WHERE userId = :userId AND isDeleted = 0 ORDER BY dueDate")
    suspend fun getItemsByUser(userId: String): List<TodoItem>

    @Query("SELECT * FROM todo_items WHERE id = :id LIMIT 1")
    suspend fun getItemById(id: String): TodoItem?

    @Query("SELECT * FROM todo_items WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedItems(userId: String): List<TodoItem>
}