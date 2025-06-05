package com.cipherxzc.whatsnext.data.database

import androidx.room.TypeConverter
import com.google.firebase.Timestamp
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Timestamp?): Long? {
        return value?.seconds?.times(1000) // 转为毫秒
    }

    @TypeConverter
    fun toTimestamp(value: Long?): Timestamp? {
        return value?.let { Timestamp(Date(it)) }
    }
}