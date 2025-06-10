package com.cipherxzc.whatsnext.ui.main.assistant.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cipherxzc.whatsnext.data.database.TodoItemInfo
import com.cipherxzc.whatsnext.ui.core.viewmodel.TodoDataViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ChatEntry {
    data class UserMessage(val text: String) : ChatEntry()
    data class AiMessage(val text: String) : ChatEntry()
    data class ItemsInfo(val items: List<Pair<Int, TodoItemInfo>>) : ChatEntry()
}

class AssistantViewModel(
    private val azureViewModel: AzureViewModel,
    private val todoDataViewModel: TodoDataViewModel
) : ViewModel() {
    private var currentId = 0

    private val _history = MutableStateFlow<List<ChatEntry>>(emptyList())
    val history: StateFlow<List<ChatEntry>> = _history.asStateFlow()

    private val _inputFlow = MutableStateFlow(TextFieldValue(""))
    val inputFlow: StateFlow<TextFieldValue> = _inputFlow.asStateFlow()

    fun setInput(input: TextFieldValue) {
        _inputFlow.value = input
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    fun initAssistant() {
        azureViewModel.initChat()
    }

    private fun assignId(): Int {
        currentId = currentId + 1
        return currentId
    }

    fun sendMessage() {
        val userPrompt = inputFlow.value.text.trim()
        if (userPrompt.isEmpty()) {
            return
        }

        _history.value = _history.value + ChatEntry.UserMessage(userPrompt)
        _inputFlow.value = TextFieldValue("") // 清空输入框

        viewModelScope.launch {
            val result = azureViewModel.chat(userPrompt)

            if (result == null) {
                // 超时或网络错误
                _history.value = _history.value + ChatEntry.AiMessage("⚠️ 请求超时，请稍后再试")
                return@launch
            }

            val (newItems, reply) = result

            _history.value = _history.value + ChatEntry.AiMessage(reply)

            if (newItems.isNotEmpty()) {
                val newIdItems = newItems.map { item -> assignId() to item }
                _history.value = _history.value + ChatEntry.ItemsInfo(newIdItems)
            }
        }
    }

    private fun deleteItem(id: Int) {
        val currentHistory = _history.value
        _history.value = currentHistory.mapNotNull { entry ->
            when (entry) {
                is ChatEntry.ItemsInfo -> {
                    val filteredItems = entry.items.filterNot { it.first == id }
                    if (filteredItems.isNotEmpty()) ChatEntry.ItemsInfo(filteredItems) else null
                }
                else -> entry
            }
        }
    }

    fun acceptItem(id: Int) {
        viewModelScope.launch {
            val currentHistory = _history.value
            val itemToAccept = currentHistory.flatMap { entry ->
                if (entry is ChatEntry.ItemsInfo) entry.items else emptyList()
            }.find { it.first == id }

            itemToAccept?.let { (_, todoItemInfo) ->
                todoDataViewModel.insertItem(todoItemInfo)
                deleteItem(id)
            }
        }
    }

    fun dismissItem(id: Int) {
        deleteItem(id)
    }

    // TODO: 修改其他 ViewModel 也使用 companion object
    companion object {
        fun factory(
            azureViewModel: AzureViewModel,
            todoDataViewModel: TodoDataViewModel
        ) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    require(modelClass == AssistantViewModel::class.java)
                    return AssistantViewModel(azureViewModel, todoDataViewModel) as T
                }
            }
    }
}