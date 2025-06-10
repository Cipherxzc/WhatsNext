package com.cipherxzc.whatsnext.ui.main.todolist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.data.database.TodoItemInfo
import com.cipherxzc.whatsnext.ui.core.common.LoadingScreen
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.TodoListViewModel
import com.cipherxzc.whatsnext.ui.main.utils.CardType
import com.cipherxzc.whatsnext.ui.main.utils.ItemCard
import com.cipherxzc.whatsnext.ui.main.utils.TodoItemPreview

@Composable
fun TodoList(
    todoListViewModel: TodoListViewModel,
    navigateDetail: (String) -> Unit
) {
    val isLoading by todoListViewModel.isLoadingFlow.collectAsStateWithLifecycle()

    val overdueItems by todoListViewModel.overdueItemsFlow.collectAsStateWithLifecycle()
    val todoItems by todoListViewModel.todoItemsFlow.collectAsStateWithLifecycle()
    val completedItems by todoListViewModel.completedItemsFlow.collectAsStateWithLifecycle()

    val overdueExpend by todoListViewModel.overdueExpendFlow.collectAsStateWithLifecycle()
    val todoExpend by todoListViewModel.todoExpendFlow.collectAsStateWithLifecycle()
    val completedExpend by todoListViewModel.completedExpendFlow.collectAsStateWithLifecycle()

    val previewItem = remember { mutableStateOf<TodoItemInfo?>(null) }

    if (isLoading) {
        LoadingScreen("TodoList")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            collapsibleItemList(
                title = "⏰ 逾期任务",
                items = overdueItems,
                expanded = overdueExpend,
                type = CardType.Complete,
                onToggleExpand = { todoListViewModel.expand("overdue") },
                onItemClicked = { previewItem.value = it.toInfo() },
                onLongPress = { navigateDetail(it.id) },
                onDismiss = { todoListViewModel.reset(it.id) },
                onDelete = { todoListViewModel.deleteItem(it.id) }
            )

            collapsibleItemList(
                title = "📝 待完成任务",
                items = todoItems,
                expanded = todoExpend,
                type = CardType.Complete,
                onToggleExpand = { todoListViewModel.expand("todo") },
                onItemClicked = { previewItem.value = it.toInfo() },
                onLongPress = { navigateDetail(it.id) },
                onDismiss = { todoListViewModel.reset(it.id) },
                onDelete = { todoListViewModel.deleteItem(it.id) }
            )

            collapsibleItemList(
                title = "✅ 已完成任务",
                items = completedItems,
                expanded = completedExpend,
                type = CardType.Reset,
                onToggleExpand = { todoListViewModel.expand("completed") },
                onItemClicked = { previewItem.value = it.toInfo() },
                onLongPress = { navigateDetail(it.id) },
                onDismiss = { todoListViewModel.reset(it.id) },
                onDelete = { todoListViewModel.deleteItem(it.id) }
            )
        }
    }

    previewItem.value?.let { item ->
        TodoItemPreview(
            item = item,
            onDismiss = { previewItem.value = null }
        )
    }
}

internal fun LazyListScope.collapsibleItemList(
    title: String,
    items: List<TodoItem>,
    expanded: Boolean,
    type: CardType,
    onToggleExpand: () -> Unit,
    onItemClicked: (TodoItem) -> Unit,
    onLongPress: (TodoItem) -> Unit,
    onDismiss: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit
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
                item = item.toInfo(),
                onItemClicked = { onItemClicked(item) },
                onLongPress = { onLongPress(item) },
                onDismiss = { onDismiss(item) },
                onDelete = { onDelete(item) },
                type = type
            )
        }
    }
}