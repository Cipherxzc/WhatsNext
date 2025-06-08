package com.cipherxzc.whatsnext.ui.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.data.repository.AzureRepository
import com.cipherxzc.whatsnext.ui.core.viewmodel.TodoDataViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AzureViewModel(
    private val todoDataViewModel: TodoDataViewModel
) : ViewModel() {

    private val azureRepository = AzureRepository()

    suspend fun whatsNext(): List<Pair<TodoItem, String>> {
        val allItems = todoDataViewModel.getAllItems()
        val todoItems = allItems.filter { !it.isCompleted }

        azureRepository.clearPrompt()

        azureRepository.append("Todo list:\n")
        azureRepository.append(todoItems)

        val timeNow = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(Date())

        val systemPrompt = """
        You are a personal task-prioritization assistant.
        Current local time: $timeNow.
    
        Input: a JSON array of todo items.
        Each item has: id, title, detail, dueDate (yyyy-MM-dd HH:mm or null), importance (0-10 or null).
    
        Evaluate the tasks holistically — urgency, long-term value, estimated effort,
        personal motivation, and any hints from title/detail. 
        Missing fields (null) mean you need to infer their relevance.
    
        Return ONLY a JSON array, each element:
        { "id": "<item id>", "reason": "<≤25 words>" }
    
        List up to THREE items in the order you recommend tackling them.
        """.trimIndent()

        val response = azureRepository.sendToLLM(systemPrompt = systemPrompt)

        return try {
            @Serializable data class Choice(val id: String, val reason: String)
            val choices = Json.decodeFromString<List<Choice>>(response)

            choices.mapNotNull { c ->
                todoItems.find { it.id == c.id }?.let { it to c.reason }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class AzureViewModelFactory(
    private val todoDataViewModel: TodoDataViewModel
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == AzureViewModel::class.java)
        return AzureViewModel(todoDataViewModel) as T
    }
}