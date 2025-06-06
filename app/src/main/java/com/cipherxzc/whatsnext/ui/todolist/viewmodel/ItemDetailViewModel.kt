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

class ItemDetailViewModel(
    private val todoDataViewModel: TodoDataViewModel,
    private val itemId: String
) : ViewModel() {

    private var isModified: Boolean = false

    private val _isLoadingFlow = MutableStateFlow(false)
    val isLoadingFlow: StateFlow<Boolean> = _isLoadingFlow

    private val _titleFlow = MutableStateFlow(TextFieldValue(""))
    private val _detailFlow = MutableStateFlow(TextFieldValue(""))
    private val _dueDateFlow = MutableStateFlow<Date?>(null)
    private val _isCompletedFlow = MutableStateFlow<Boolean>(false)

    val titleFlow = _titleFlow
    val detailFlow = _detailFlow
    val dueDateFlow = _dueDateFlow
    val isCompletedFlow = _isCompletedFlow

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

    fun withdraw() {
        _isCompletedFlow.value = false
        isModified = true
    }

    fun updateTitle(newTitle: TextFieldValue) {
        _titleFlow.value = newTitle
        isModified = true
    }

    fun updateDetail(newDetail: TextFieldValue) {
        _detailFlow.value = newDetail
        isModified = true
    }

    fun updateDueDate(newDueDate: Date) {
        _dueDateFlow.value = newDueDate
        isModified = true
    }
}

class ItemDetailViewModelFactory(
    private val todoDataViewModel: TodoDataViewModel,
    private val itemId: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ItemDetailViewModel::class.java)
        return ItemDetailViewModel(
            todoDataViewModel = todoDataViewModel,
            itemId = itemId
        ) as T
    }
}