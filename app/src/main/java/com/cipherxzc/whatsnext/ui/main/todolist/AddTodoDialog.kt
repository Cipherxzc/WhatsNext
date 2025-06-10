package com.cipherxzc.whatsnext.ui.main.todolist

import android.widget.Toast
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.AddTodoViewModel
import com.cipherxzc.whatsnext.ui.main.utils.DatePicker
import com.cipherxzc.whatsnext.ui.main.utils.ImportanceDropdownMenu

@Composable
fun AddTodoDialog(
    addTodoViewModel: AddTodoViewModel
){
    val showDialog by addTodoViewModel.showDialogFlow.collectAsStateWithLifecycle()

    val newTitle by addTodoViewModel.titleFlow.collectAsStateWithLifecycle()
    val newDetail by addTodoViewModel.detailFlow.collectAsStateWithLifecycle()
    val newDueDate by addTodoViewModel.dueDateFlow.collectAsStateWithLifecycle()
    val newImportance by addTodoViewModel.importanceFlow.collectAsStateWithLifecycle()

    var showCalendar by remember { mutableStateOf(false) }

    // 当 showAddDialog 为 true 时显示 AlertDialog 对话框
    if (showDialog) {
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
                        label = { Text("标题") },
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
                        label = { Text("详细描述") },
                        placeholder = { Text("详细描述") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ImportanceDropdownMenu(
                        importance = newImportance,
                        setImportance = addTodoViewModel::setImportance
                    )

                    DatePicker(
                        date = newDueDate,
                        onDateSelected = { addTodoViewModel.setDueDate(it) }
                    )
                }
            },
            confirmButton = {
                val context = LocalContext.current
                TextButton(
                    onClick = {
                        Toast.makeText(
                            context,
                            addTodoViewModel.addTodo(),
                            Toast.LENGTH_SHORT
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