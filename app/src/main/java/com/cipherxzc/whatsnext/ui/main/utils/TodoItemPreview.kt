package com.cipherxzc.whatsnext.ui.main.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.data.database.TodoItemInfo
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TodoItemPreview(
    item: TodoItemInfo,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape            = MaterialTheme.shapes.large,
        containerColor   = MaterialTheme.colorScheme.surface,
        tonalElevation   = 4.dp,
        title = {
            Text(
                text  = item.title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                item.dueDate?.let {
                    AssistChip(
                        onClick = {},
                        leadingIcon = {
                            Icon(Icons.Outlined.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                        },
                        label = {
                            Text(dateFormat.format(it))
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            labelColor     = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }

                item.importance?.let { imp ->
                    val impLabel = when {
                        imp >= 8 -> "🔥 高"
                        imp >= 5 -> "👍 中"
                        else     -> "⌛ 低"
                    }
                    AssistChip(
                        onClick = {},
                        leadingIcon = {
                            Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.onSecondary)
                        },
                        label = { Text("重要度：$impLabel ($imp)") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            labelColor     = MaterialTheme.colorScheme.onSecondary
                        )
                    )
                }

                if (item.detail.isNotBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp),
                        tonalElevation = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
                    ) {
                        Text(
                            text  = item.detail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    Text(
                        text = "（暂无补充说明）",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}