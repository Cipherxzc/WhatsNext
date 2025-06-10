package com.cipherxzc.whatsnext.ui.main.assistant.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cipherxzc.whatsnext.data.database.TodoItem
import com.cipherxzc.whatsnext.data.database.TodoItemInfo
import com.cipherxzc.whatsnext.data.repository.AzureRepository
import com.cipherxzc.whatsnext.data.repository.ChatHistory
import com.cipherxzc.whatsnext.ui.core.viewmodel.TodoDataViewModel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class LLMResponse(
    val reply: String,
    val newItems: List<TodoItemInfo>
)

class AzureViewModel(
    private val todoDataViewModel: TodoDataViewModel
) : ViewModel() {

    private val azureRepository = AzureRepository()

    private val whatsNextHistory = ChatHistory()
    private val chatHistory = ChatHistory()

    fun clearChatHistory() {
        chatHistory.clear()
    }

    fun initWhatsNext() {
        val systemPrompt = """
        You are a personal task-prioritization assistant.
    
        ## Inputs
        1. Current local time ("yyyy-MM-dd HH:mm").
        2. A JSON array of todo items.
        3. User instructions in natural language (Optional).
        
        Each todo item contains:
        - id (string)
        - title (string)
        - detail (string)
        - dueDate (string, "yyyy-MM-dd HH:mm" or null)
        - importance (integer, 0–10 or null)
    
        ## Your Job
        Evaluate the tasks holistically — urgency, long-term value, estimated effort,
        personal motivation, and any hints from title/detail. 
        Missing fields (null) mean you need to infer their relevance.
    
        ## Output FORMAT
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

        val response = azureRepository.sendToLLM(
            chatHistory = whatsNextHistory
        )

        if (response == "timeout"){
            return null
        }

        whatsNextHistory.add(azureRepository.getUserPrompt(), response)

        return try {
            @Serializable data class Choice(val id: String, val reason: String)
            val choices = Json.decodeFromString<List<Choice>>(response)

            choices.mapNotNull { c ->
                todoItems.find { it.id == c.id }?.let { it to c.reason }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun initChat() {
        val systemPrompt = """
        You are TaskMate, a helpful AI assistant embedded in an Android todo-list app.
        
        ## Inputs
        1. Current local time (Asia/Tokyo, ISO-8601): **{{CURRENT_TIME}}**
        2. A JSON array named **todos** that lists all current todo items:
           **{{TODO_JSON}}**
        3. Optional user instruction text (may be empty).
        
        Each todo item has:
        - id        (string, unique)
        - title     (string)
        - detail    (string)
        - dueDate   ("yyyy-MM-dd HH:mm" string or null)
        - importance(integer 0–10 or null)
        
        ## Your job
        * Read the existing todos and the user’s instruction.
        * Reply conversationally to the user (same language they used).
        * If the user implicitly or explicitly asks to add a new task, or if adding a task would clearly help (e.g. they say “remind me next week”), include the new task(s) in your output.
        
        ## Output FORMAT (MUST be valid JSON, nothing else):
        ```json
        {
          "reply": "<your natural-language response to the user>",
          "newItems": [
            {
              "title": "<task title>",
              "detail": "<task description>",
              "dueDate": "<"yyyy-MM-dd HH:mm" or null>",
              "importance": <0-10>
            }
            // … more items as needed
          ]
        }
        ```
        """.trimIndent()

        azureRepository.setSystemPrompt(systemPrompt)
    }

    suspend fun chat(
        userPrompt: String
    ): Pair<List<TodoItemInfo>, String>? {
        azureRepository.clearPrompt()

        azureRepository.appendCurrentTime()

        val allItems = todoDataViewModel.getAllItems()
        val todoItems = allItems.filter { !it.isCompleted }

        azureRepository.append("Todo list:\n")
        azureRepository.append(todoItems)

        azureRepository.append(userPrompt)

        val response = azureRepository.sendToLLM(
            asJson = true,
            chatHistory = chatHistory
        )

        if (response == "timeout"){
            return null
        }

        chatHistory.add(azureRepository.getUserPrompt(), response)

        return try {
            val decoded = Json.decodeFromString<LLMResponse>(response)

            decoded.newItems to decoded.reply
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList<TodoItemInfo>() to "Error parsing response: ${e.message}"
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