package com.cipherxzc.whatsnext.ui.todolist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cipherxzc.whatsnext.ui.assistant.viewmodel.AzureViewModel
import com.cipherxzc.whatsnext.ui.core.viewmodel.SyncViewModel
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.AddTodoViewModel
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.AddTodoViewModelFactory
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    userName: String,
    todoListViewModel: TodoListViewModel,
    azureViewModel: AzureViewModel,
    syncViewModel: SyncViewModel,
    onItemClicked: (String) -> Unit,
    onLogout: () -> Unit
) {
    val addTodoViewModel: AddTodoViewModel = viewModel(
        factory = AddTodoViewModelFactory(todoListViewModel::insertItem)
    )

    val isSyncing by syncViewModel.isSyncing.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // TODO: 重新修改 layout
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),   // 处理状态栏/导航栏
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // TODO: 后续改为 What's Next? 并将新增移动到最上方
            ExtendedFloatingActionButton(
                text = { Text("有什么要做的？") },
                icon = { Icon(Icons.Filled.Add, null) },
                onClick = { addTodoViewModel.showDialog() },
                expanded = true,
                containerColor = MaterialTheme.colorScheme.primary,
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // 同步按钮：加载时灰掉并带指示器
                    FilledTonalButton(
                        onClick = {
                            syncViewModel.sync(
                                onError = {
                                    snackbarHostState.showSnackbar(
                                        message = "同步失败：${it.message}"
                                    )
                                },
                                onComplete = {
                                    snackbarHostState.showSnackbar("同步完成！")
                                    todoListViewModel.loadItems()
                                }
                            )
                        },
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TodoList(
                todoListViewModel = todoListViewModel,
                onItemClicked = onItemClicked
            )

            AddTodoDialog(addTodoViewModel)
        }
    }
}