package com.cipherxzc.whatsnext.ui.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.ui.core.viewmodel.TodoDataViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

class TodoListViewModel(
    private val todoDataViewModel: TodoDataViewModel
) : ViewModel() {

    private val _overdueItemsFlow = MutableStateFlow<List<TodoItem>>(emptyList())
    val overdueItemsFlow: StateFlow<List<TodoItem>> = _overdueItemsFlow

    private val _todoItemsFlow = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoItemsFlow: StateFlow<List<TodoItem>> = _todoItemsFlow

    private val _completedItemsFlow = MutableStateFlow<List<TodoItem>>(emptyList())
    val completedItemsFlow: StateFlow<List<TodoItem>> = _completedItemsFlow

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: StateFlow<Boolean> = _isLoadingFlow

    init {
        loadItems()
    }

    fun loadItems() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingFlow.value = true

            val allItems = todoDataViewModel.getAllItems()

            val overdueItems = allItems.filter { !it.isCompleted && it.isOverdue() }
            val todoItems = allItems.filter { !it.isCompleted && !it.isOverdue() }
            val completedItems = allItems.filter { it.isCompleted }

            _overdueItemsFlow.value = overdueItems
            _todoItemsFlow.value = todoItems
            _completedItemsFlow.value = completedItems

            _isLoadingFlow.value = false
        }
    }

    fun insertItem(title: String, detail: String = "", dueDate: Date? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = todoDataViewModel.insertItem(title, detail, dueDate?.let { Timestamp(it) })
            loadItems()
        }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDataViewModel.deleteItem(itemId)
            loadItems()
        }
    }

    fun complete(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDataViewModel.completeItem(itemId)
            loadItems()
        }
    }

    fun withdraw(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDataViewModel.uncompleteItem(itemId)
            loadItems()
        }
    }
}

class TodoListViewModelFactory(
    private val todoDataViewModel: TodoDataViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == TodoListViewModel::class.java)
        return TodoListViewModel(todoDataViewModel) as T
    }
}