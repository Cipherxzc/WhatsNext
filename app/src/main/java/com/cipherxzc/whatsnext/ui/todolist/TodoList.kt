package com.cipherxzc.whatsnext.ui.todolist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoListViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TodoList(
    todoListViewModel: TodoListViewModel,
    onItemClicked: (String) -> Unit
) {
    val isLoading by todoListViewModel.isLoadingFlow.collectAsState()

    val overdueItems by todoListViewModel.overdueItemsFlow.collectAsState()
    val todoItems by todoListViewModel.todoItemsFlow.collectAsState()
    val completedItems by todoListViewModel.completedItemsFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        CollapsibleItemList(
            title = "⏰ 逾期任务",
            items = overdueItems,
            onItemClicked = onItemClicked,
            onDismiss = { todoListViewModel.complete(it) },
            type = BackgroundType.Complete
        )

        CollapsibleItemList(
            title = "📝 待完成任务",
            items = todoItems,
            onItemClicked = onItemClicked,
            onDismiss = { todoListViewModel.complete(it) },
            type = BackgroundType.Complete
        )

        CollapsibleItemList(
            title = "✅ 已完成任务",
            items = completedItems,
            onItemClicked = onItemClicked,
            onDismiss = { todoListViewModel.reset(it) },
            type = BackgroundType.Reset
        )
    }
}

internal enum class BackgroundType {
    Complete, // 绿底 + 勾
    Reset     // 黄底 + 重置图标
}

@Composable
internal fun CollapsibleItemList(
    title: String,
    items: List<TodoItem>,
    onItemClicked: (String) -> Unit,
    onDismiss: (String) -> Unit,
    type: BackgroundType
) {
    var expanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
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

        AnimatedVisibility(  // 添加动画
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            LazyColumn(
                modifier = Modifier
                    .wrapContentHeight()
                    .heightIn(max = Dp.Infinity)
                    .fillMaxWidth(),
                userScrollEnabled = false
            ) {
                items(items, key = {it.id}) { item ->
                    ItemCard(
                        modifier = Modifier.animateItem(),
                        item = item,
                        onItemClicked = { onItemClicked(item.id) },
                        onDismiss = { onDismiss(item.id) },
                        type = type
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ItemCard(
    modifier: Modifier = Modifier,
    item: TodoItem,
    onItemClicked: () -> Unit,
    onDismiss: () -> Unit,
    type: BackgroundType
) {
    // 根据类型选择背景色和图标
    val (backgroundColor, icon) = when (type) {
        BackgroundType.Complete ->
            Color(0xFF4CAF50) to Icons.Default.Check
        BackgroundType.Reset ->
            Color(0xFFFFC107) to Icons.Default.Refresh
    }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA) }

    // 左滑过程中的滑动比例，用于做背景透明度等动画
    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (it == DismissValue.DismissedToEnd) {
                onDismiss()
            }
            false
        }
    )
    val progress by animateFloatAsState(
        targetValue = dismissState.progress.fraction
    )


    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.StartToEnd),
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor.copy(alpha = 0.5f + 0.5f * progress))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = progress)
                )
            }
        },
        dismissContent = {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable(onClick = onItemClicked)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 标题占比大部分
                    Text(
                        text = item.title,
                        modifier = Modifier.weight(1f)
                    )
                    // 如果有截止日期就显示
                    item.dueDate?.let { ts ->
                        Text(
                            text = dateFormat.format(ts.toDate()),
                        )
                    }
                }
            }
        }
    )
}