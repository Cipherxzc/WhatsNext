package com.cipherxzc.whatsnext.ui.main.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.data.database.TodoItemInfo
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TodoItemPreview(
    item: TodoItemInfo,
    showDialog: Boolean,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(item.title) },
            text = {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    item.dueDate?.let {
                        val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(it)
                        Text(text = "截止日期: $formattedDate")
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    item.importance?.let {
                        Text(text = "Importance: $it")
                    }
                    Text(text = item.detail)
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("关闭")
                }
            }
        )
    }
}