package com.cipherxzc.whatsnext.data.repository

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.aallam.openai.client.OpenAIHost
import com.cipherxzc.whatsnext.BuildConfig
import com.cipherxzc.whatsnext.data.database.TodoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class AzureTodoItemDto(
    val title: String,
    val detail: String,
    val dueDate: String? = null, // ISO-8601 格式
    val importance: Int = 0
)

class AzureRepository() {
    private val azureKey = BuildConfig.AZURE_OPENAI_API_KEY
    private val azureEndpoint = BuildConfig.AZURE_OPENAI_ENDPOINT
    private val deploymentId = BuildConfig.AZURE_OPENAI_DEPLOYMENT_ID
    private val apiVersion = BuildConfig.AZURE_OPENAI_API_VERSION

    private val client: OpenAI = OpenAI(
        config = OpenAIConfig(
            token = azureKey,
            host = OpenAIHost.azure(
                resourceName = azureEndpoint,
                deploymentId = deploymentId,
                apiVersion = apiVersion
            )
        )
    )

    private fun TodoItem.toPromptJson(): String {
        val promptItem = AzureTodoItemDto(
            title = title,
            detail = detail,
            dueDate = dueDate?.toDate()?.toString(), // 可用 ISO-8601 日期格式
            importance = importance
        )
        return Json.encodeToString(promptItem)
    }

    private val promptBuilder = StringBuilder()

    fun clearPrompt() = promptBuilder.clear()

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

    suspend fun sendToLLM(
        systemPrompt: String? = null,
        maxTokens: Int = 512
    ): String = withContext(Dispatchers.IO) {

        val userPrompt = promptBuilder.toString().trim()

        val request = ChatCompletionRequest(
            model = ModelId(deploymentId),
            messages = buildList {
                if (systemPrompt != null) add(ChatMessage(ChatRole.System, systemPrompt))
                add(ChatMessage(ChatRole.User, userPrompt))
            },
            maxCompletionTokens = maxTokens,
            temperature = 0.7
        )

        val response = client.chatCompletion(request)
        response.choices.first().message.content.toString()
    }
}