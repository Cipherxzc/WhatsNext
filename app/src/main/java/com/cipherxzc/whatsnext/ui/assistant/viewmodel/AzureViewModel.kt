package com.cipherxzc.whatsnext.ui.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.data.repository.AzureRepository
import com.cipherxzc.whatsnext.ui.core.viewmodel.TodoDataViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AzureViewModel(
    private val todoDataViewModel: TodoDataViewModel
) : ViewModel() {

    private val azureRepository = AzureRepository()

    fun initWhatsNext() {
        azureRepository.clearHistory()

        // system prompt
        val systemPrompt = """
        You are a personal task-prioritization assistant.
    
        You will receive three inputs:
        1. Current local time ("yyyy-MM-dd HH:mm").
        2. A JSON array of todo items.
        3. User instructions in natural language (Optional).
        
        Each todo item contains:
        - id (string)
        - title (string)
        - detail (string)
        - dueDate (string, "yyyy-MM-dd HH:mm" or null)
        - importance (integer, 0–10 or null)
    
        Evaluate the tasks holistically — urgency, long-term value, estimated effort,
        personal motivation, and any hints from title/detail. 
        Missing fields (null) mean you need to infer their relevance.
    
        Return ONLY a JSON array, each element:
        { "id": "<item id>", "reason": "<≤25 words>" }
    
        List up to THREE items in the order you recommend tackling them.
        """.trimIndent()

        azureRepository.setSystemPrompt(systemPrompt)
    }

    suspend fun whatsNext(
        userPrompt: String? = null
    ): List<Pair<TodoItem, String>>? {
        azureRepository.clearPrompt()

        azureRepository.appendCurrentTime()

        val allItems = todoDataViewModel.getAllItems()
        val todoItems = allItems.filter { !it.isCompleted }

        azureRepository.append("Todo list:\n")
        azureRepository.append(todoItems)

        userPrompt?.let {
            azureRepository.append(it)
        }

        // get response
        val response = azureRepository.sendToLLM(asJson = true)

        if (response == "timeout"){
            return null
        }

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