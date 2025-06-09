package com.cipherxzc.whatsnext.ui.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.ui.assistant.viewmodel.AzureViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WhatsNextViewModel(
    private val azureViewModel: AzureViewModel
) : ViewModel() {

    private val _recommendedItemsFlow = MutableStateFlow<List<Pair<TodoItem, String>>>(emptyList())
    val recommendedItemsFlow: StateFlow<List<Pair<TodoItem, String>>> = _recommendedItemsFlow.asStateFlow()

    private val _showDialogFlow = MutableStateFlow(false)
    val showDialogFlow: StateFlow<Boolean> = _showDialogFlow.asStateFlow()

    fun whatsNext(userPrompt: String?) {
        fetchRecommendations(userPrompt)
    }

    fun showDialog() {
        _recommendedItemsFlow.value = emptyList()
        azureViewModel.initWhatsNext()
        _showDialogFlow.value = true
    }

    fun hideDialog() {
        _showDialogFlow.value = false
    }

    private fun fetchRecommendations(prompt: String?) {
        viewModelScope.launch {
            val result = azureViewModel.whatsNext(prompt)
            if (result != null && result.isNotEmpty()) {
                _recommendedItemsFlow.value = result
                _showDialogFlow.value = true
            } else {
                // TODO: 错误处理
                // 好像什么都不做也可以，超时 == 什么都不发生
            }
        }
    }
}

class WhatsNextViewModelFactory(
    private val azureViewModel: AzureViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == WhatsNextViewModel::class.java)
        return WhatsNextViewModel(azureViewModel) as T
    }
}