package com.cipherxzc.whatsnext.data.repository

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.cipherxzc.whatsnext.BuildConfig
import com.cipherxzc.whatsnext.data.database.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Locale

@Serializable
private data class AzureTodoItemDto(
    val id: String,
    val title: String,
    val detail: String,
    val dueDate: String? = null, // ISO-8601 格式
    val importance: Int? = null
)

class AzureRepository() {
    private val azureKey = BuildConfig.AZURE_OPENAI_API_KEY
    private val azureResourceName = BuildConfig.AZURE_OPENAI_RESOURCE_NAME
    private val deploymentId = BuildConfig.AZURE_OPENAI_DEPLOYMENT_ID
    private val apiVersion = BuildConfig.AZURE_OPENAI_API_VERSION

    private val client: OpenAI = OpenAI(
        config = OpenAIConfig(
            token = azureKey,
            host = OpenAIHost.azure(
                resourceName = azureResourceName,
                deploymentId = deploymentId,
                apiVersion = apiVersion
            )
        )
    )

    private fun TodoItem.toPromptJson(): String {
        val dueDateStr: String? = dueDate
            ?.toDate()
            ?.let { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA).format(it) }
        val promptItem = AzureTodoItemDto(
            id = id,
            title = title,
            detail = detail,
            dueDate = dueDateStr,
            importance = importance
        )
        return Json.encodeToString(promptItem)
    }

    private val promptBuilder = StringBuilder()
    private var systemPrompt: String = ""

    fun clearPrompt() = promptBuilder.clear()
    fun setSystemPrompt(prompt: String) {
        systemPrompt = prompt
    }

    // 以纯文本追加到 prompt（自动换行）
    fun append(text: String) = promptBuilder.appendLine(text.trimEnd())

    // 将 TodoItem 列表格式化为英文条目并追加到 prompt
    fun append(items: List<TodoItem>) {
        val indent = "  "
        val jsonArray = items.joinToString(
            separator = ",\n",
            prefix = "[\n",
            postfix = "\n]"
        ) { item ->
            indent + item.toPromptJson()
        }

        promptBuilder.appendLine(jsonArray)
    }

    private val history: ArrayDeque<Pair<String, String>> = ArrayDeque()
    private var maxHistory = 10

    fun clearHistory() = history.clear()

    private fun trimHistory() {
        while (history.size > maxHistory) {
            history.removeFirst()
        }
    }

    private fun addHistory(user: String, assistant: String) {
        history.addLast(user to assistant)
        trimHistory()
    }

    suspend fun sendToLLM(
        maxTokens: Int = 512,
        temperature: Double = 0.7,
        timeoutMillis: Long = 10000L,
        asJson: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val userPrompt = promptBuilder.toString().trim()

        val messages = buildList {
            add(ChatMessage(ChatRole.System, systemPrompt))

            // history
            history.forEach { (user, assistant) ->
                add(ChatMessage(ChatRole.User, user))
                add(ChatMessage(ChatRole.Assistant, assistant))
            }

            add(ChatMessage(ChatRole.User, userPrompt))
        }

        val request = ChatCompletionRequest(
            model = ModelId(deploymentId),
            messages = messages,
            maxCompletionTokens = maxTokens,
            temperature = temperature,
            responseFormat = if (asJson) ChatResponseFormat.JsonObject else null
        )

        try {
            val response = withTimeout(timeoutMillis) {
                client.chatCompletion(request)
            }

            val reply = response.choices.first().message.content.orEmpty()

            // 请求成功后写入历史
            addHistory(userPrompt, reply)

            reply
        } catch (e: TimeoutCancellationException) {
            "timeout"
        }
    }
}