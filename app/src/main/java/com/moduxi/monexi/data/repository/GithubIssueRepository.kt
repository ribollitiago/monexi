package com.moduxi.monexi.data.repository

import com.moduxi.monexi.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

data class IssueResponse(
    val number: Int,
    val url: String,
    val title: String,
    val state: String
)

class GithubIssueRepository(
    private val authRepository: AuthRepository,
    private val baseUrl: String = "https://apifinance.moduxi.com"
) {

    suspend fun createIssue(type: String, title: String, body: String? = null): Result<IssueResponse> =
        withContext(Dispatchers.IO) {
            try {
                val token = authRepository.currentUser
                    ?.getIdToken(false)?.await()?.token
                    ?: return@withContext Result.failure(Exception("Usuário não autenticado."))

                val url = URL("$baseUrl/github/issues")
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Authorization", "Bearer $token")
                    setRequestProperty("Content-Type", "application/json")
                    doOutput = true
                    connectTimeout = 15_000
                    readTimeout = 15_000
                }

                val payload = JSONObject().apply {
                    put("type", type)
                    put("title", title)
                    if (!body.isNullOrBlank()) put("body", body)
                }

                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use {
                    it.write(payload.toString())
                }

                val responseCode = connection.responseCode
                val responseBody = if (responseCode in 200..299) {
                    connection.inputStream.bufferedReader().use { it.readText() }
                } else {
                    val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    return@withContext Result.failure(
                        Exception(parseErrorMessage(responseCode, errorBody))
                    )
                }

                val json = JSONObject(responseBody)
                Result.success(
                    IssueResponse(
                        number = json.getInt("number"),
                        url = json.getString("url"),
                        title = json.getString("title"),
                        state = json.getString("state")
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun parseErrorMessage(code: Int, body: String): String = when (code) {
        401 -> "Sessão expirada. Faça login novamente."
        429 -> "Limite de envios atingido. Tente novamente mais tarde."
        503 -> "Serviço indisponível no momento."
        else -> try {
            JSONObject(body).optString("message", "Erro ao enviar ($code).")
        } catch (_: Exception) {
            "Erro ao enviar ($code)."
        }
    }
}
