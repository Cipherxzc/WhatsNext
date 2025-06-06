package com.cipherxzc.whatsnext.ui.todolist.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.Date

class AddTodoViewModel(
    val insertItem: (String, String, Date?) -> Unit
) : ViewModel() {

    private val _showDialogFlow = MutableStateFlow(false)
    val showDialogFlow: StateFlow<Boolean> = _showDialogFlow

    fun showDialog() = _showDialogFlow.update { true }
    fun hideDialog() = _showDialogFlow.update { false }
}

class AddTodoViewModelFactory(
    val insertItem: (String, String, Date?) -> Unit
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == AddTodoViewModel::class.java)
        return AddTodoViewModel(insertItem) as T
    }
}