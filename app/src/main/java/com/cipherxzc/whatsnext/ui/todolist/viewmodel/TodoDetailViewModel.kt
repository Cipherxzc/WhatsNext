package com.cipherxzc.whatsnext.ui.todolist.viewmodel

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cipherxzc.whatsnext.ui.core.viewmodel.TodoDataViewModel
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class TodoDetailViewModel(
    private val todoDataViewModel: TodoDataViewModel,
    private val itemId: String
) : ViewModel() {

    private var isModified: Boolean = false

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: StateFlow<Boolean> = _isLoadingFlow

    private val _titleFlow = MutableStateFlow(TextFieldValue(""))
    private val _detailFlow = MutableStateFlow(TextFieldValue(""))
    private val _dueDateFlow = MutableStateFlow<Date?>(null)
    private val _importanceFlow = MutableStateFlow<Int?>(null)
    private val _isCompletedFlow = MutableStateFlow<Boolean>(false)

    val titleFlow: StateFlow<TextFieldValue> = _titleFlow
    val detailFlow: StateFlow<TextFieldValue> = _detailFlow
    val dueDateFlow: StateFlow<Date?> = _dueDateFlow
    val importanceFlow: StateFlow<Int?> = _importanceFlow
    val isCompletedFlow: StateFlow<Boolean> = _isCompletedFlow

    init {
        loadItem()
    }

    fun loadItem() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingFlow.value = true

            saveItem()

            val todoItem = todoDataViewModel.getItem(itemId)
            if (todoItem == null) {
                throw IllegalArgumentException("Item with ID $itemId not found")
            }
            _titleFlow.value = TextFieldValue(todoItem.title)
            _detailFlow.value = TextFieldValue(todoItem.detail)
            _dueDateFlow.value = todoItem.dueDate?.toDate()
            _isCompletedFlow.value = todoItem.isCompleted
            _importanceFlow.value = todoItem.importance

            _isLoadingFlow.value = false
        }
    }

    fun saveItem() {
        if (isModified){
            todoDataViewModel.updateItem(
                id = itemId,
                title = titleFlow.value.text,
                detail = detailFlow.value.text,
                dueDate = dueDateFlow.value?.let { Timestamp(it) },
                importance = importanceFlow.value,
                isCompleted = isCompletedFlow.value
            )
            isModified = false
        }
        // 结束时会保证 isModified = false
    }

    fun complete() {
        _isCompletedFlow.value = true
        isModified = true
    }

    fun reset() {
        _isCompletedFlow.value = false
        isModified = true
    }

    fun setTitle(newTitle: TextFieldValue) {
        _titleFlow.value = newTitle
        isModified = true
    }

    fun setDetail(newDetail: TextFieldValue) {
        _detailFlow.value = newDetail
        isModified = true
    }

    fun setDueDate(newDueDate: Date) {
        _dueDateFlow.value = newDueDate
        isModified = true
    }

    fun setImportance(newImportance: Int?) {
        _importanceFlow.value = newImportance
        isModified = true
    }
}

class TodoDetailViewModelFactory(
    private val todoDataViewModel: TodoDataViewModel,
    private val itemId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == TodoDetailViewModel::class.java)
        return TodoDetailViewModel(
            todoDataViewModel = todoDataViewModel,
            itemId = itemId
        ) as T
    }
}