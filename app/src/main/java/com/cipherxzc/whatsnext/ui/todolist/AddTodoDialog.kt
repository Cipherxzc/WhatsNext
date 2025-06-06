package com.cipherxzc.whatsnext.ui.todolist

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.AddTodoViewModel

@Composable
fun AddTodoDialog(
    addTodoViewModel: AddTodoViewModel
){
    var newTitle by remember { mutableStateOf("") }
    var newDetail by remember { mutableStateOf("") }

    val showDialogState by addTodoViewModel.showDialogFlow.collectAsState()

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
                        onValueChange = { newTitle = it },
                        label = { Text("Name") },
                        placeholder = { Text("Enter item name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.small,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        value = newItemDescription,
                        onValueChange = { newItemDescription = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Enter description") }
                    )
                }
            },
            confirmButton = {
                val context = androidx.compose.ui.platform.LocalContext.current
                TextButton(
                    onClick = {
                        // 当用户输入的 name 非空时，进行添加操作
                        if (newItemName.isNotBlank()) {
                            addTodoViewModel.insertItem(
                                name = newItemName,
                                description = if (newItemDescription.isBlank()) null else newItemDescription
                            )
                            newItemName = ""
                            newItemDescription = ""
                            addTodoViewModel.hideDialog()
                        } else {
                            android.widget.Toast.makeText(context, "请输入打卡项名称", android.widget.Toast.LENGTH_SHORT).show()
                            return@TextButton
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // 取消操作，清空输入并关闭对话框
                        newItemName = ""
                        newItemDescription = ""
                        addTodoViewModel.hideDialog()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}