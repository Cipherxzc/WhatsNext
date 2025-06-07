package com.cipherxzc.whatsnext.ui.todolist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoDetailViewModel
import com.cipherxzc.whatsnext.ui.core.common.LoadingScreen
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.material3.OutlinedTextField

@Composable
fun TodoDetailScreen(todoDetailViewModel: TodoDetailViewModel) {
    val isLoading by todoDetailViewModel.isLoadingFlow.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            todoDetailViewModel.saveItem()
        }
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
            BottomAppBar {
                // TODO: 专注时间
            }
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
                    Text(
                        text = dueDate?.let { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(it) } ?: "设置截止日期",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable {
                            // TODO: 打开 DatePickerDialog
                        }
                    )

                    val buttonText = if (isCompleted) "已完成" else "标记完成"
                    Button(
                        onClick = {
                            if (isCompleted) todoDetailViewModel.withdraw()
                            else             todoDetailViewModel.complete()
                        }
                    ) { Text(buttonText) }
                }

                Divider()

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { newTitle ->
                        todoDetailViewModel.setTitle(newTitle)
                    },
                    placeholder = { Text("打算做点什么？") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )

                // Detail
                OutlinedTextField(
                    value = detail,
                    onValueChange = { newDetail ->
                        todoDetailViewModel.setDetail(newDetail)
                    },
                    placeholder = { Text("详细描述") },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}