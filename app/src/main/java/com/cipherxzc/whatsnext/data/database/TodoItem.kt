package com.cipherxzc.whatsnext.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp


@Entity(tableName = "todo_items")
data class TodoItem (
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val detail: String = "",
    val dueDate: Timestamp? = null,
    val importance: Int = 0, // 0 ~ 10
    val isCompleted: Boolean = false,
    // 同步信息
    val lastModified: Timestamp = Timestamp.now(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
) {
    fun isOverdue(): Boolean {
        return dueDate != null && dueDate < Timestamp.now()
    }
}