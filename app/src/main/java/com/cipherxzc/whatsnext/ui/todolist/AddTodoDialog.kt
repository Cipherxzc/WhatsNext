package com.cipherxzc.whatsnext.ui.todolist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.AddTodoViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AddTodoDialog(
    addTodoViewModel: AddTodoViewModel
){
    val showDialogState by addTodoViewModel.showDialogFlow.collectAsState()

    val newTitle by addTodoViewModel.titleFlow.collectAsState()
    val newDetail by addTodoViewModel.detailFlow.collectAsState()
    val newDueDate by addTodoViewModel.dueDateFlow.collectAsState()

    // 当 showAddDialog 为 true 时显示 AlertDialog 对话框
    if (showDialogState) {
        AlertDialog(
            onDismissRequest = addTodoViewModel::hideDialog,
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("添加新的打卡项") },
            text = {
                Column {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        value = newTitle,
                        onValueChange = { addTodoViewModel.setTitle(it) },
                        label = { Text("Title") },
                        placeholder = { Text("准备做点什么？") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        value = newDetail,
                        onValueChange = { addTodoViewModel.setDetail(it) },
                        label = { Text("Detail") },
                        placeholder = { Text("详细描述") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = newDueDate?.let { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(it) } ?: "设置截止日期",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable {
                            // TODO: 打开 DatePickerDialog
                        }
                    )
                }
            },
            confirmButton = {
                val context = androidx.compose.ui.platform.LocalContext.current
                TextButton(
                    onClick = {
                        android.widget.Toast.makeText(
                            context,
                            addTodoViewModel.addTodo(),
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                ) {
                    Text("添加")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { addTodoViewModel.hideDialog() }
                ) {
                    Text("取消")
                }
            }
        )
    }
}