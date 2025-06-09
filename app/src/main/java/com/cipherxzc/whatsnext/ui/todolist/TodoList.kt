package com.cipherxzc.whatsnext.ui.todolist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.ui.core.common.LoadingScreen
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoListViewModel

@Composable
fun TodoList(
    todoListViewModel: TodoListViewModel,
    onItemClicked: (String) -> Unit,
    onAddTodoClicked: () -> Unit,
) {
    val isLoading by todoListViewModel.isLoadingFlow.collectAsState()

    val overdueItems by todoListViewModel.overdueItemsFlow.collectAsState()
    val todoItems by todoListViewModel.todoItemsFlow.collectAsState()
    val completedItems by todoListViewModel.completedItemsFlow.collectAsState()

    val overdueExpend by todoListViewModel.overdueExpendFlow.collectAsState()
    val todoExpend by todoListViewModel.todoExpendFlow.collectAsState()
    val completedExpend by todoListViewModel.completedExpendFlow.collectAsState()

    if (isLoading) {
        LoadingScreen("TodoList")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Button(
            onClick = onAddTodoClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "添加任务",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加任务")
        }

        Spacer(modifier = Modifier.height(8.dp))

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