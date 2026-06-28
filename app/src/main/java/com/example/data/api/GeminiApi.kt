package com.example.data.api

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import android.util.Log

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class ResponseFormatText(
    @Json(name = "mimeType") val mimeType: String
)

@JsonClass(generateAdapter = true)
data class ResponseFormat(
    @Json(name = "text") val text: ResponseFormatText? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "temperature") val temperature: Float? = null,
    @Json(name = "responseFormat") val responseFormat: ResponseFormat? = null
)

@JsonClass(generateAdapter = true)
data class GeminiTool(
    @Json(name = "googleSearch") val googleSearch: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null,
    @Json(name = "tools") val tools: List<GeminiTool>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

interface GeminiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: GeminiService = retrofit.create(GeminiService::class.java)

    suspend fun fetchAnalysis(prompt: String, systemPrompt: String = "You are Sharrow.ai - Your Personal AI Market Analyst."): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiApi", "API Key is missing or placeholder!")
            return "Error: Missing Gemini API Key in AI Studio secrets. Please add it to unlock advanced custom intelligence!"
        }

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(temperature = 0.5f)
        )

        return try {
            val response = service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "No analysis feedback generated. Please try again."
        } catch (e: Exception) {
            Log.e("GeminiApi", "Error calling Gemini API", e)
            "AI Service temporarily busy. [Status: Offline Demo Mode active if key missing]. Local intelligence computed. Details: ${e.localizedMessage}"
        }
    }

    suspend fun fetchSearchNews(symbols: List<String>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("GeminiApi", "API Key is missing or placeholder!")
            return "[]"
        }

        val prompt = """
            Search the web using Google Search for the latest financial news, stock market updates, and corporate announcements regarding the following tickers/companies: ${symbols.joinToString(", ")}.
            
            Based on the search results, output exactly a JSON array containing up to 5 news articles.
            Each JSON object MUST contain exactly these fields:
            - "id": A unique string ID starting with "live_n_" followed by a number, e.g., "live_n_1"
            - "source": The news source name, e.g., "Bloomberg", "Reuters", "Economic Times"
            - "title": A crisp, highly accurate headline about the company's recent development
            - "baseSummary": A concise 2-3 sentence summary of the news story and what happened.
            - "relevanceToUser": Explain why this is highly relevant to a holder of this stock (e.g., "High (You hold ${symbols.firstOrNull() ?: ""})")
            - "bullishScore": An integer score from 0 to 100 indicating how positive this news is
            - "bearishScore": An integer score from 0 to 100 indicating how negative this news is
            
            IMPORTANT: Return ONLY the raw JSON array. DO NOT wrap it in any markdown code block (like ```json ... ```). Start response with [ and end with ].
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            systemInstruction = Content(parts = listOf(Part(text = "You are a live financial news agent. You MUST use Google Search grounding to retrieve real, actual, current stock news headlines from the live web for the requested tickers. DO NOT hallucinate news. Format the news items perfectly into the requested JSON schema."))),
            generationConfig = GenerationConfig(temperature = 0.2f),
            tools = listOf(GeminiTool(googleSearch = emptyMap()))
        )

        return try {
            val response = service.generateContent(
                model = "gemini-3.5-flash",
                apiKey = apiKey,
                request = request
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "[]"
        } catch (e: Exception) {
            Log.e("GeminiApi", "Error calling Gemini Search API", e)
            "[]"
        }
    }
}
