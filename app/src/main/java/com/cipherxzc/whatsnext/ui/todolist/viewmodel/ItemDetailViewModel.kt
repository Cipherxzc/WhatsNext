package com.cipherxzc.whatsnext.ui.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.ui.core.viewmodel.TodoDataViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ItemDetailViewModel(
    private val todoDataViewModel: TodoDataViewModel
) : ViewModel() {

    private val _itemFlow = MutableStateFlow<TodoItem?>(null)
    val itemFlow: StateFlow<TodoItem?> = _itemFlow

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: StateFlow<Boolean> = _isLoadingFlow

    private var isModified: Boolean = false

    fun loadItem(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingFlow.value = true

            _itemFlow.value = todoDataViewModel.getItem(itemId)
            isModified = false

            _isLoadingFlow.value = false
        }
    }

    fun saveItem() {
        if (isModified){
            _itemFlow.value?.let {
                todoDataViewModel.updateItem(it)
                isModified = false
            }
        }
    }

    fun complete() {
        _itemFlow.value = _itemFlow.value?.copy(isCompleted = true)
        isModified = true
    }

    fun withdraw() {
        _itemFlow.value = _itemFlow.value?.copy(isCompleted = false)
        isModified = true
    }
}

class ItemDetailViewModelFactory(
    private val todoDataViewModel: TodoDataViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ItemDetailViewModel::class.java)
        return ItemDetailViewModel(todoDataViewModel) as T
    }
}