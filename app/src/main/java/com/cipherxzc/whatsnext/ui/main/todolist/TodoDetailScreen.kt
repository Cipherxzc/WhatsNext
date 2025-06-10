package com.cipherxzc.whatsnext.ui.main.todolist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.ui.core.common.LoadingScreen
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.TodoDetailViewModel
import com.cipherxzc.whatsnext.ui.main.utils.DatePicker
import com.cipherxzc.whatsnext.ui.main.utils.ImportanceDropdownMenu

@Composable
fun TodoDetailScreen(
    todoDetailViewModel: TodoDetailViewModel,
    onBack: () -> Unit
) {
    val isLoading by todoDetailViewModel.isLoadingFlow.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            todoDetailViewModel.saveItem()
        }
    }

    BackHandler {
        onBack()
    }

    if (isLoading){
        LoadingScreen()
    } else {
        ItemDetailContent(todoDetailViewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ItemDetailContent(todoDetailViewModel: TodoDetailViewModel) {
    val title       = todoDetailViewModel.titleFlow.collectAsState().value
    val detail      = todoDetailViewModel.detailFlow.collectAsState().value
    val dueDate     = todoDetailViewModel.dueDateFlow.collectAsState().value
    val importance  = todoDetailViewModel.importanceFlow.collectAsState().value
    val isCompleted = todoDetailViewModel.isCompletedFlow.collectAsState().value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "任务详情")
                }
            )
        },
        bottomBar = {
            // TODO: 专注时间
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()) // 内容超出时可滚动
            ) {

                // 顶部：dueDate + 完成按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DatePicker(
                        date = dueDate,
                        onDateSelected = { date ->
                            todoDetailViewModel.setDueDate(date)
                        }
                    )

                    val buttonText = if (isCompleted) "已完成" else "标记完成"
                    Button(
                        onClick = {
                            if (isCompleted) todoDetailViewModel.reset()
                            else             todoDetailViewModel.complete()
                        }
                    ) { Text(buttonText) }
                }

                HorizontalDivider()

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { newTitle ->
                        todoDetailViewModel.setTitle(newTitle)
                    },
                    label = { Text("标题") },
                    placeholder = { Text("打算做点什么？") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )

                ImportanceDropdownMenu(
                    importance = importance,
                    setImportance = todoDetailViewModel::setImportance
                )

                // Detail
                OutlinedTextField(
                    value = detail,
                    onValueChange = { newDetail ->
                        todoDetailViewModel.setDetail(newDetail)
                    },
                    label = { Text("详细描述") },
                    placeholder = { Text("详细描述") },
                    modifier = Modifier.fillMaxSize().weight(1f),
                    singleLine = false,
                    maxLines = Int.MAX_VALUE
                )
            }
        }
    }
}