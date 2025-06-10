package com.cipherxzc.whatsnext.ui.main.assistant.viewmodel

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

    private val _history = MutableStateFlow<MutableList<ChatEntry>>(mutableListOf())
    val history: StateFlow<List<ChatEntry>> = _history.asStateFlow()

    fun clearHistory() {
        _history.value = mutableListOf()
    }

    private fun assignId(): Int {
        currentId = currentId + 1
        return currentId
    }

    fun sendMessage(userPrompt: String) {
        _history.value.addLast(ChatEntry.UserMessage(userPrompt))

        viewModelScope.launch {
            val result = azureViewModel.chat(userPrompt)

            if (result == null) {
                // 超时或网络错误
                _history.value.addLast(ChatEntry.AiMessage("⚠️ 请求超时，请稍后再试"))
                return@launch
            }

            val (newItems, reply) = result

            _history.value.addLast(ChatEntry.AiMessage(reply))

            if (newItems.isNotEmpty()) {
                val newIdItems = newItems.map { item -> assignId() to item }
                _history.value.addLast(ChatEntry.ItemsInfo(newIdItems))
            }
        }
    }

    private fun deleteItem(id: Int) {
        val currentHistory = _history.value
        val updatedHistory = currentHistory.mapNotNull { entry ->
            when (entry) {
                is ChatEntry.ItemsInfo -> {
                    val filteredItems = entry.items.filterNot { it.first == id }
                    if (filteredItems.isNotEmpty()) ChatEntry.ItemsInfo(filteredItems) else null
                }
                else -> entry
            }
        }
        _history.value = updatedHistory as MutableList<ChatEntry>
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