package com.cipherxzc.whatsnext.ui.main.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.data.database.TodoItemInfo
import java.text.SimpleDateFormat
import java.util.Locale

enum class CardType {
    Complete, // 绿底 + 勾
    Reset     // 黄底 + 重置图标
}

// TODO: 使用 AnchoredDraggable 替代 SwipeToDismiss 实现更复杂的滑动交互
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    item: TodoItemInfo,
    type: CardType,
    onItemClicked: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onDelete: (() -> Unit)? = null,
    content: @Composable () -> Unit = {}
) {
    // 根据类型选择背景色和图标
    val (backgroundColor, icon) = when (type) {
        CardType.Complete ->
            Color(0xFF4CAF50) to Icons.Default.Check
        CardType.Reset ->
            Color(0xFFFFC107) to Icons.Default.Refresh
    }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA) }

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.7f },          // 与旧代码保持一致
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onDismiss() // 右滑：完成/还原
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete?.invoke() // 左滑：删除（可选）
                }

                else -> null
            }
            false
        }
    )

    Card(
        modifier = modifier
            .padding(horizontal = 0.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = onItemClicked,
                onLongClick = onLongPress
            )
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = onDelete != null,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val progress  = dismissState.progress
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        // 右滑背景
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backgroundColor.copy(alpha = 0.5f + 0.5f * progress))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(
                                icon, null,
                                tint = Color.White.copy(alpha = if (progress > .7f) 1f else 0f)
                            )
                        }
                    }
                    SwipeToDismissBoxValue.EndToStart -> {
                        // 左滑背景
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Red.copy(alpha = 0.5f + 0.5f * progress))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete, "delete",
                                tint = Color.White.copy(alpha = if (progress > .7f) 1f else 0f)
                            )
                        }
                    }
                    else -> {}
                }
            }
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.title, modifier = Modifier.weight(1f))
                    item.dueDate?.let { ts ->
                        Text(dateFormat.format(ts))
                    }
                }
                content()   // 透传可扩展内容
            }
        }
    }
}