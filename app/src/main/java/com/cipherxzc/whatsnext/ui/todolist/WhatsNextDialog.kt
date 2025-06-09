package com.cipherxzc.whatsnext.ui.todolist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoListViewModel
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.WhatsNextViewModel

@Composable
fun WhatsNextDialog(
    todoListViewModel: TodoListViewModel,
    whatsNextViewModel: WhatsNextViewModel,
    onItemClicked: (String) -> Unit
){
    val showDialog by whatsNextViewModel.showDialogFlow.collectAsState()
    val recommendedItems by whatsNextViewModel.recommendedItemsFlow.collectAsState()

    val userPrompt = remember { mutableStateOf("") }

    // 当 showAddDialog 为 true 时显示 AlertDialog 对话框
    if (showDialog) {
        AlertDialog(
            onDismissRequest = whatsNextViewModel::hideDialog,
            shape = MaterialTheme.shapes.large,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("What's Next?") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp) // 限高 + 滚动
                        .verticalScroll(rememberScrollState())
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            shape = MaterialTheme.shapes.small,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            value = userPrompt.value,
                            onValueChange = { userPrompt.value = it },
                            label = { Text("Prompt") },
                            placeholder = { Text("有什么想法？") }
                        )
                        TextButton(
                            modifier = Modifier.wrapContentWidth(),
                            onClick = { whatsNextViewModel.whatsNext(userPrompt.value) },
                        ) {
                            Text("发送")
                        }
                    }

                    recommendedItems.forEach { (item, reason) ->
                        ItemCard(
                            item = item,
                            type = CardType.Complete,
                            onItemClicked = { onItemClicked(item.id) },
                            onDismiss = { todoListViewModel.complete(item.id) },
                            onDelete = { todoListViewModel.deleteItem(item.id) },
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    text = "Reason:",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    text = reason,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = whatsNextViewModel::hideDialog,
                ) {
                    Text("返回")
                }
            }
        )
    }
}