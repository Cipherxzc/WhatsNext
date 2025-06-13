package com.cipherxzc.whatsnext.ui.main.assistant

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(
    assistantViewModel: AssistantViewModel
) {
    val history by assistantViewModel.history.collectAsStateWithLifecycle(emptyList())
    val input by assistantViewModel.inputFlow.collectAsStateWithLifecycle(TextFieldValue(""))

    val previewItem = remember { mutableStateOf<TodoItemInfo?>(null) }

    val listState: LazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "任务助手")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
                )
            )
        },
        bottomBar = {
            InputBar(
                value = input,
                onValueChange = { assistantViewModel.setInput(it) },
                onSend = {
                    assistantViewModel.sendMessage()
                    focusManager.clearFocus()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ){
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

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp, horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                }
                            )
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

    // 自动滚动到最新消息
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(history.size) {
        coroutineScope.launch {
            listState.animateScrollToItem(maxOf(history.size - 1, 0))
        }
    }
}

@Composable
private fun InputBar(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("输入消息…") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            val enabled = value.text.isNotBlank()
            IconButton(
                onClick = onSend,
                enabled = enabled,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    tint = if (enabled)
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UserBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            shape = BubbleShape(isMe = true),
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = 2.dp
        ) {
            Text(
                text,
                Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .animateContentSize(),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun AiBubble(text: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Surface(
            shape = BubbleShape(isMe = false),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 1.dp
        ) {
            Text(
                text,
                Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .animateContentSize(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

private fun BubbleShape(isMe: Boolean) = RoundedCornerShape(
    topStart = 16.dp,
    topEnd   = 16.dp,
    bottomEnd = if (isMe) 0.dp else 16.dp,
    bottomStart = if (isMe) 16.dp else 0.dp
)

@Composable
private fun ItemsInfoList(
    entry: ChatEntry.ItemsInfo,
    assistantViewModel: AssistantViewModel,
    onPreview: (TodoItemInfo) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        entry.items.forEach { (id, item) ->
            key(id) {
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
}
