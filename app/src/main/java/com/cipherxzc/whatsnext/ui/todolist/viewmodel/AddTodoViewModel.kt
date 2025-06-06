package com.cipherxzc.whatsnext.ui.todolist.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

class AddTodoViewModel(
    private val insertItem: (String, String, Date?) -> Unit
) : ViewModel() {

    private val _showDialogFlow = MutableStateFlow(false)
    val showDialogFlow: StateFlow<Boolean> = _showDialogFlow

    private val _titleFlow = MutableStateFlow(TextFieldValue(""))
    private val _detailFlow = MutableStateFlow(TextFieldValue(""))
    private val _dueDateFlow = MutableStateFlow<Date?>(null)

    val titleFlow: StateFlow<TextFieldValue> = _titleFlow
    val detailFlow: StateFlow<TextFieldValue> = _detailFlow
    val dueDateFlow: StateFlow<Date?> = _dueDateFlow

    fun addTodo(): String {
        if (titleFlow.value.text.isNotBlank()) {
            insertItem(
                titleFlow.value.text,
                detailFlow.value.text,
                dueDateFlow.value
            )
            _titleFlow.value = TextFieldValue("")
            _detailFlow.value = TextFieldValue("")
            _dueDateFlow.value = null
            hideDialog()
            return "添加成功"
        } else {
            return "请输入Todo内容"
        }
    }

    fun showDialog(){ _showDialogFlow.value = true }
    fun hideDialog(){ _showDialogFlow.value = false }
    fun setTitle(title: TextFieldValue) { _titleFlow.value = title }
    fun setDetail(detail: TextFieldValue) { _detailFlow.value = detail }
    fun setDueDate(dueDate: Date?) { _dueDateFlow.value = dueDate  }
}

class AddTodoViewModelFactory(
    private val insertItem: (String, String, Date?) -> Unit
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == AddTodoViewModel::class.java)
        return AddTodoViewModel(insertItem) as T
    }
}