package com.cipherxzc.whatsnext.ui.todolist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.ui.core.common.DatePickerDialog
import com.cipherxzc.whatsnext.ui.core.common.LoadingScreen
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

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
    val isCompleted = todoDetailViewModel.isCompletedFlow.collectAsState().value

    var showDialog by remember { mutableStateOf(false) }

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
                            showDialog = true
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

                ImportanceMenu(todoDetailViewModel)

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

            if (showDialog) {
                DatePickerDialog(
                    onDateSelected = { date ->
                        todoDetailViewModel.setDueDate(date)
                    },
                    onDismissRequest = { showDialog = false },
                    initialDate = dueDate
                )
            }
        }
    }
}

@Composable
fun ImportanceMenu(
    todoDetailViewModel: TodoDetailViewModel
) {
    val importance  = todoDetailViewModel.importanceFlow.collectAsState().value
    var expanded by remember { mutableStateOf(false) }

    val importanceLevels = (0..10).toList()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("重要程度：", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = importance?.toString() ?: "未设置",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(Icons.Default.ArrowDropDown, contentDescription = "选择重要性")
    }

    DropdownMenu(
        modifier = Modifier
            .width(200.dp)
            .padding(8.dp),
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        importanceLevels.forEach { level ->
            Box(
                modifier = Modifier
                    .padding(vertical = 2.dp) // item 之间留缝隙
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                DropdownMenuItem(
                    text = { Text("$level") },
                    onClick = {
                        todoDetailViewModel.setImportance(level)
                        expanded = false
                    }
                )
            }
        }
    }
}