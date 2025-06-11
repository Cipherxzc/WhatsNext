package com.cipherxzc.whatsnext.ui.main.todolist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cipherxzc.whatsnext.data.database.TodoItemInfo
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.TodoListViewModel
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.WhatsNextViewModel
import com.cipherxzc.whatsnext.ui.main.utils.CardType
import com.cipherxzc.whatsnext.ui.main.utils.ItemCard
import com.cipherxzc.whatsnext.ui.main.utils.TodoItemPreview

@Composable
fun WhatsNextDialog(
    todoListViewModel: TodoListViewModel,
    whatsNextViewModel: WhatsNextViewModel,
    navigateDetail: (String) -> Unit
){
    val showDialog by whatsNextViewModel.showDialogFlow.collectAsState()
    val recommendedItems by whatsNextViewModel.recommendedItemsFlow.collectAsState()

    val userPrompt = remember { mutableStateOf("") }
    val previewItem = remember { mutableStateOf<TodoItemInfo?>(null) }

    if (showDialog) {
        AlertDialog(
            // onDismissRequest = whatsNextViewModel::hideDialog,
            onDismissRequest = {}, // 禁止点击外部关闭对话框
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
                            onClick = {
                                whatsNextViewModel.whatsNext(userPrompt.value)
                                userPrompt.value = ""
                            },
                        ) {
                            Text("发送")
                        }
                    }

                    recommendedItems.forEach { (item, reason) ->
                        key(item.id) {
                            ItemCard(
                                item = item.toInfo(),
                                type = CardType.Complete,
                                onItemClicked = { previewItem.value = item.toInfo() },
                                onLongPress = { navigateDetail(item.id) },
                                onDismiss = {
                                    todoListViewModel.complete(item.id)
                                    whatsNextViewModel.dismissRecommendation(item.id)
                                },
                                onDelete = null,
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    color  = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                                    tonalElevation = 1.dp,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Box(
                                            Modifier
                                                .width(4.dp)
                                                .heightIn(min = 36.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                                                )
                                        )

                                        Column(
                                            Modifier
                                                .weight(1f)
                                                .padding(start = 12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Info,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.tertiary
                                                )
                                                Text(
                                                    text  = "推荐理由",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    modifier = Modifier.padding(start = 4.dp)
                                                )
                                            }
                                            Text(
                                                text  = reason,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                previewItem.value?.let { item ->
                    TodoItemPreview(
                        item = item,
                        onDismiss = { previewItem.value = null }
                    )
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