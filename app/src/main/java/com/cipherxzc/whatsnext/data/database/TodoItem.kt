package com.cipherxzc.whatsnext.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "todo_items")
data class TodoItem (
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val detail: String = "",
    val dueDate: Timestamp? = null,
    val importance: Int? = null, // 0 ~ 10
    val isCompleted: Boolean = false,
    // 同步信息
    val lastModified: Timestamp = Timestamp.now(),
    val isSynced: Boolean = false,
    val isDeleted: Boolean = false,
) {
    fun isOverdue(): Boolean {
        return dueDate != null && dueDate < Timestamp.now()
    }

    fun toInfo(): TodoItemInfo {
        return TodoItemInfo(
            title = title,
            detail = detail,
            dueDate = dueDate?.toDate(),
            importance = importance
        )
    }
}

object DateSerializer : KSerializer<Date> {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(dateFormat.format(value))
    }

    override fun deserialize(decoder: Decoder): Date {
        return dateFormat.parse(decoder.decodeString())
    }
}

@Serializable
data class TodoItemInfo(
    val title: String,
    val detail: String,
    @Serializable(with = DateSerializer::class) val dueDate: Date? = null,
    val importance: Int? = null
)