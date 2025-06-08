package com.cipherxzc.whatsnext.ui.todolist

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoListViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TodoList(
    todoListViewModel: TodoListViewModel,
    onItemClicked: (String) -> Unit
) {
    val isLoading by todoListViewModel.isLoadingFlow.collectAsState()

    val overdueItems by todoListViewModel.overdueItemsFlow.collectAsState()
    val todoItems by todoListViewModel.todoItemsFlow.collectAsState()
    val completedItems by todoListViewModel.completedItemsFlow.collectAsState()

    val overdueExpend by todoListViewModel.overdueExpendFlow.collectAsState()
    val todoExpend by todoListViewModel.todoExpendFlow.collectAsState()
    val completedExpend by todoListViewModel.completedExpendFlow.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        collapsibleItemList(
            title = "⏰ 逾期任务",
            items = overdueItems,
            expanded = overdueExpend,
            type = CardType.Complete,
            onToggleExpand = { todoListViewModel.expand("overdue") },
            onItemClicked = onItemClicked,
            onDismiss = { todoListViewModel.complete(it) },
            onDelete = { todoListViewModel.deleteItem(it) }
        )

        collapsibleItemList(
            title = "📝 待完成任务",
            items = todoItems,
            expanded = todoExpend,
            type = CardType.Complete,
            onToggleExpand = { todoListViewModel.expand("todo") },
            onItemClicked = onItemClicked,
            onDismiss = { todoListViewModel.complete(it) },
            onDelete = { todoListViewModel.deleteItem(it) }
        )

        collapsibleItemList(
            title = "✅ 已完成任务",
            items = completedItems,
            expanded = completedExpend,
            type = CardType.Reset,
            onToggleExpand = { todoListViewModel.expand("completed") },
            onItemClicked = onItemClicked,
            onDismiss = { todoListViewModel.reset(it) },
            onDelete = { todoListViewModel.deleteItem(it) }
        )
    }
}

internal enum class CardType {
    Complete, // 绿底 + 勾
    Reset     // 黄底 + 重置图标
}

internal fun LazyListScope.collapsibleItemList(
    title: String,
    items: List<TodoItem>,
    expanded: Boolean,
    type: CardType,
    onToggleExpand: () -> Unit,
    onItemClicked: (String) -> Unit,
    onDismiss: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    item(key = "header-$title") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleExpand() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = if (expanded) "折叠" else "展开"
            )
        }
    }

    if (expanded) {
        items(items, key = { "$title-${it.id}" }) { item ->
            ItemCard(
                modifier = Modifier.animateItem(),
                item = item,
                onItemClicked = { onItemClicked(item.id) },
                onDismiss = { onDismiss(item.id) },
                onDelete = { onDelete(item.id) },
                type = type
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ItemCard(
    modifier: Modifier = Modifier,
    item: TodoItem,
    type: CardType,
    onItemClicked: () -> Unit,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
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

    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onItemClicked)
    ) {
        SwipeToDismiss(
            state = dismissState,
            directions = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart),
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
                            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = progress))
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
                            Icon(Icons.Default.Delete, contentDescription = "delete", tint = Color.White.copy(alpha = progress))
                        }
                    }
                    else -> {}
                }
            },
            dismissContent = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        modifier = Modifier.weight(1f)
                    )
                    item.dueDate?.let { ts ->
                        Text(
                            text = dateFormat.format(ts.toDate()),
                        )
                    }
                }
            }
        )
    }
}