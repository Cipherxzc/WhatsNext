package com.cipherxzc.whatsnext.ui.main.utils

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
// TODO: 使用 material3 替代
@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun ItemCard(
    modifier: Modifier = Modifier,
    item: TodoItemInfo,
    type: CardType,
    onItemClicked: () -> Unit = {},
    onLongPress: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onDelete: () -> Unit = {},
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

    // 右滑过程中的滑动比例，用于做背景透明度等动画
    val dismissState = rememberDismissState { value ->
        when (value) {
            DismissValue.DismissedToEnd -> {
                onDismiss()
            }
            DismissValue.DismissedToStart -> {
                onDelete()
            }
            else -> {
                // Do nothing
            }
        }
        false
    }
    val progress by animateFloatAsState(dismissState.progress.fraction)
    val dismissThreshold = remember { 0.6f }

    Card(
        modifier = modifier
            .padding(horizontal = 0.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = onItemClicked,
                onLongClick = onLongPress
            )
    ) {
        SwipeToDismiss(
            modifier = Modifier.fillMaxWidth(),
            state = dismissState,
            directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
            dismissThresholds = { direction -> FractionalThreshold(dismissThreshold) },
            background = {
                val direction = dismissState.dismissDirection
                when (direction) {
                    DismissDirection.StartToEnd -> {
                        // 右滑背景（完成/还原）
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(backgroundColor.copy(alpha = 0.5f + 0.5f * progress))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = if (progress > dismissThreshold) 1f else 0f)
                            )
                        }
                    }

                    DismissDirection.EndToStart -> {
                        // 左滑背景（删除）
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Red.copy(alpha = 0.5f + 0.5f * progress))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "delete",
                                tint = Color.White.copy(alpha = if (progress > dismissThreshold) 1f else 0f)
                            )
                        }
                    }

                    else -> {}
                }
            },
            dismissContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.title,
                            modifier = Modifier.weight(1f)
                        )
                        item.dueDate?.let { ts ->
                            Text(
                                text = dateFormat.format(ts),
                            )
                        }
                    }

                    content()
                }
            }
        )
    }
}