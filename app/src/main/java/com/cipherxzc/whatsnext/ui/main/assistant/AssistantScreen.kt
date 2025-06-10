package com.cipherxzc.whatsnext.ui.main.assistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cipherxzc.whatsnext.data.database.TodoItemInfo
import com.cipherxzc.whatsnext.ui.main.assistant.viewmodel.AssistantViewModel
import com.cipherxzc.whatsnext.ui.main.assistant.viewmodel.ChatEntry
import com.cipherxzc.whatsnext.ui.main.utils.CardType
import com.cipherxzc.whatsnext.ui.main.utils.ItemCard
import com.cipherxzc.whatsnext.ui.main.utils.TodoItemPreview
import kotlinx.coroutines.launch

@Composable
fun AssistantScreen(
    assistantViewModel: AssistantViewModel
) {
    val history by assistantViewModel.history.collectAsStateWithLifecycle(emptyList())
    val input by assistantViewModel.inputFlow.collectAsStateWithLifecycle(TextFieldValue(""))

    val previewItem = remember { mutableStateOf<TodoItemInfo?>(null) }
    val showPreview = remember { mutableStateOf(false) }

    val listState: LazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { assistantViewModel.clearHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Clear History")
                }
            }
        },
        bottomBar = {
            AssistantInputBar(
                value = input,
                onValueChange = { assistantViewModel.setInput(it) },
                onSend = {
                    assistantViewModel.sendMessage()
                    focusManager.clearFocus()
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(history) { entry ->
                when (entry) {
                    is ChatEntry.UserMessage -> UserBubble(entry.text)
                    is ChatEntry.AiMessage -> AiBubble(entry.text)
                    is ChatEntry.ItemsInfo ->
                        ItemsInfoList(
                            entry = entry,
                            assistantViewModel = assistantViewModel,
                            onPreview = { item ->
                                previewItem.value = item
                                showPreview.value = true
                            }
                        )
                }
            }
        }
    }

    previewItem.value?.let { item ->
        TodoItemPreview(
            item = item,
            showDialog = showPreview.value,
            onDismiss = { previewItem.value = null }
        )
    }

    // 自动滚动到最新消息
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(history.size) {
        coroutineScope.launch {
            listState.animateScrollToItem(maxOf(history.size - 1, 0))
        }
    }
}

@Composable
private fun AssistantInputBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit
) {
    Surface(shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text("输入消息…") },
                singleLine = true,
                maxLines = 1,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSend() })
            )
            IconButton(onClick = onSend) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "发送")
            }
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun AiBubble(text: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 1.dp
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun ItemsInfoList(
    entry: ChatEntry.ItemsInfo,
    assistantViewModel: AssistantViewModel,
    onPreview: (TodoItemInfo) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        entry.items.forEach { (id, item) ->
            ItemCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                item = item,
                type = CardType.Complete,
                onItemClicked = { onPreview(item) },
                onDismiss = { assistantViewModel.acceptItem(id) },
                onDelete = { assistantViewModel.dismissItem(id) }
            )
        }
    }
}
