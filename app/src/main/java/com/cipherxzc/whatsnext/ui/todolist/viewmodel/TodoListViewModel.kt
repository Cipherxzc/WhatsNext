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
import java.util.Date

class TodoListViewModel(
    private val todoDataViewModel: TodoDataViewModel
) : ViewModel() {

    private val _overdueItemsFlow = MutableStateFlow<List<TodoItem>>(emptyList())
    private val _todoItemsFlow = MutableStateFlow<List<TodoItem>>(emptyList())
    private val _completedItemsFlow = MutableStateFlow<List<TodoItem>>(emptyList())

    val overdueItemsFlow: StateFlow<List<TodoItem>> = _overdueItemsFlow
    val todoItemsFlow: StateFlow<List<TodoItem>> = _todoItemsFlow
    val completedItemsFlow: StateFlow<List<TodoItem>> = _completedItemsFlow

    val _overdueExpendFlow = MutableStateFlow(true)
    val _todoExpendFlow = MutableStateFlow(true)
    val _completedExpendFlow = MutableStateFlow(false)

    val overdueExpendFlow: StateFlow<Boolean> = _overdueExpendFlow
    val todoExpendFlow: StateFlow<Boolean> = _todoExpendFlow
    val completedExpendFlow: StateFlow<Boolean> = _completedExpendFlow

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

    fun insertItem(title: String, detail: String = "", dueDate: Date? = null, importance: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = todoDataViewModel.insertItem(title, detail, dueDate, importance)
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

    fun reset(itemId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            todoDataViewModel.unCompleteItem(itemId)
            loadItems()
        }
    }

    fun expand(type: String) {
        when (type) {
            "overdue" -> _overdueExpendFlow.value = !_overdueExpendFlow.value
            "todo" -> _todoExpendFlow.value = !_todoExpendFlow.value
            "completed" -> _completedExpendFlow.value = !_completedExpendFlow.value
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