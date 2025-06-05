package com.cipherxzc.whatsnext.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp


@Entity(tableName = "todo_items")
data class TodoItem (
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String? = null,
    val dueDate: Timestamp? = null,
    val isCompleted: Boolean = false,
    val lastModified: Timestamp = Timestamp.now(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
)