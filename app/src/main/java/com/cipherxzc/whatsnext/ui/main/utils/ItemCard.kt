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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.data.database.TodoItemInfo
import java.text.SimpleDateFormat
import java.util.Date
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
    val (accentColor, icon) = when (type) {
        CardType.Complete ->
            Color(0xFFB0E174) to Icons.Default.Check
        CardType.Reset ->
            Color(0xFFFFEC61) to Icons.Default.Refresh
    }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA) }

    val dismissState = rememberSwipeToDismissBoxState(
        positionalThreshold = { it * 0.7f },
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onDismiss()
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete?.invoke()
                }

                else -> null
            }
            false
        }
    )

    Card(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = onItemClicked,
                onLongClick = onLongPress
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = onDelete != null,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> {
                        // 右滑背景
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            accentColor.copy(alpha = 0.5f),
                                            accentColor
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "Complete",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    SwipeToDismissBoxValue.EndToStart -> {
                        // 左滑背景
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.error,
                                            MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.onError
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
                        MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                        shape = MaterialTheme.shapes.medium
                    )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    item.dueDate?.let {
                        Text(
                            text = dateFormat.format(it),
                            style = MaterialTheme.typography.bodySmall,
                            color =
                                if (it > Date()) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.error,
                        )
                    }
                }

                content()
            }
        }
    }
}