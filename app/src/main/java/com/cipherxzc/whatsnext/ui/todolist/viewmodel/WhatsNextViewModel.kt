package com.cipherxzc.whatsnext.ui.todolist.viewmodel

import androidx.lifecycle.ViewModel
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

    private val _recommendedItems = MutableStateFlow<List<Pair<TodoItem, String>>>(emptyList())
    val recommendedItems: StateFlow<List<Pair<TodoItem, String>>> = _recommendedItems.asStateFlow()

    private val _showRecommend = MutableStateFlow(false)
    val showRecommend: StateFlow<Boolean> = _showRecommend.asStateFlow()

    fun whatsNext(userPrompt: String?) {
        fetchRecommendations(userPrompt)
    }

    fun show() {
        _recommendedItems.value = emptyList()
        azureViewModel.initWhatsNext()
        _showRecommend.value = true
    }

    fun hidden() {
        _showRecommend.value = false
    }

    private fun fetchRecommendations(prompt: String?) {
        viewModelScope.launch {
            val result = azureViewModel.whatsNext(prompt)
            if (result != null && result.isNotEmpty()) {
                _recommendedItems.value = result
                _showRecommend.value = true
            } else {
                // TODO: 错误处理
                // 好像什么都不做也可以，超时 == 什么都不发生
            }
        }
    }
}