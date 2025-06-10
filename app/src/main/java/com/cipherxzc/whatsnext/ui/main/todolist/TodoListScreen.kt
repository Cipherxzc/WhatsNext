package com.cipherxzc.whatsnext.ui.main.todolist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cipherxzc.whatsnext.ui.core.viewmodel.SyncViewModel
import com.cipherxzc.whatsnext.ui.main.assistant.viewmodel.AzureViewModel
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.AddTodoViewModel
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.AddTodoViewModelFactory
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.TodoListViewModel
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.WhatsNextViewModel
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.WhatsNextViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    todoListViewModel: TodoListViewModel,
    azureViewModel: AzureViewModel,
    syncViewModel: SyncViewModel,
    navigateDetail: (String) -> Unit
) {
    val addTodoViewModel: AddTodoViewModel = viewModel(
        factory = AddTodoViewModelFactory(todoListViewModel::insertItem)
    )
    val whatsNextViewModel: WhatsNextViewModel = viewModel(
        factory = WhatsNextViewModelFactory(azureViewModel)
    )

    val snackbarHostState = remember { SnackbarHostState() }

    // TODO: 重新修改 layout
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),   // 处理状态栏/导航栏
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("What's Next？") },
                icon = { Icon(Icons.Filled.CheckCircle, null) },
                onClick = { whatsNextViewModel.showDialog() },
                expanded = true,
                containerColor = MaterialTheme.colorScheme.primary,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                TopButtons(
                    snackbarHostState = snackbarHostState,
                    showDialog = addTodoViewModel::showDialog,
                    loadItems = todoListViewModel::loadItems,
                    syncViewModel = syncViewModel
                )

                TodoList(
                    todoListViewModel = todoListViewModel,
                    navigateDetail = navigateDetail
                )
            }

            AddTodoDialog(addTodoViewModel)

            WhatsNextDialog(
                todoListViewModel = todoListViewModel,
                whatsNextViewModel = whatsNextViewModel,
                navigateDetail = navigateDetail
            )
        }
    }
}

@Composable
private fun TopButtons(
    snackbarHostState: SnackbarHostState,
    showDialog: () -> Unit,
    loadItems: () -> Unit,
    syncViewModel: SyncViewModel
) {
    val isSyncing by syncViewModel.isSyncing.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = showDialog,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "添加任务",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("添加任务")
        }

        Button(
            onClick = {
                syncViewModel.sync(
                    onError = {
                        snackbarHostState.showSnackbar(
                            message = "同步失败：${it.message}"
                        )
                    },
                    onComplete = {
                        snackbarHostState.showSnackbar("同步完成！")
                        loadItems()
                    }
                )
            },
            modifier = Modifier.weight(0.6f),
            enabled = !isSyncing
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(18.dp)
                        .padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
                Text("同步中…")
            } else {
                Icon(Icons.Outlined.Refresh, null)
                Spacer(Modifier.width(6.dp))
                Text("同步")
            }
        }
    }
}