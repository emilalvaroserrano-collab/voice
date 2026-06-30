package com.example.api

import android.util.Log
import com.example.BuildConfig
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

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    
    // Model selection
    const val VOICE_MODEL = "gemini-2.5-flash-native-audio-preview-12-2025"
    const val TEXT_MODEL = "gemini-3.5-flash"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Call Gemini to generate a response (text + voice audio).
     */
    suspend fun askGemini(
        prompt: String,
        voiceName: String = "Kore", // Kore, Puck, Charon, Fenrir, Aoede
        userAudioBase64: String? = null,
        history: List<Content> = emptyList()
    ): Pair<String, String?> {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API Key is missing. Using beautiful simulated voice assistant responses.")
            return getSimulatedResponse(prompt)
        }

        // Prepare conversation parts
        val newParts = mutableListOf<Part>()
        if (userAudioBase64 != null) {
            // Send user spoken audio directly
            newParts.add(Part(inlineData = InlineData(mimeType = "audio/wav", data = userAudioBase64)))
        }
        if (prompt.isNotEmpty()) {
            newParts.add(Part(text = prompt))
        }

        val newContent = Content(role = "user", parts = newParts)
        val fullContents = history + listOf(newContent)

        // Request audio output
        val request = GenerateContentRequest(
            contents = fullContents,
            generationConfig = GenerationConfig(
                responseModalities = listOf("TEXT", "AUDIO"),
                speechConfig = SpeechConfig(
                    voiceConfig = VoiceConfig(
                        prebuiltVoiceConfig = PrebuiltVoiceConfig(voiceName = voiceName)
                    )
                ),
                temperature = 0.7f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are Aura, an elite personal voice assistant. Speak with modern warmth, clarity, and precision. Keep responses relatively short, poetic, and highly conversational."))
            )
        )

        return try {
            val response = service.generateContent(VOICE_MODEL, apiKey, request)
            val candidate = response.candidates?.firstOrNull()
            val responseContent = candidate?.content
            
            var textResponse = ""
            var audioBase64Response: String? = null

            responseContent?.parts?.forEach { part ->
                if (part.text != null) {
                    textResponse += part.text
                }
                if (part.inlineData != null) {
                    audioBase64Response = part.inlineData.data
                }
            }

            if (textResponse.isEmpty()) {
                textResponse = "Aura listened to you."
            }

            Pair(textResponse, audioBase64Response)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call failed, falling back to simulated response", e)
            getSimulatedResponse(prompt)
        }
    }

    private fun getSimulatedResponse(prompt: String): Pair<String, String?> {
        val lower = prompt.lowercase()
        val text = when {
            lower.contains("hello") || lower.contains("hi") -> 
                "Greetings. I am Aura, your vocal intelligence. Shadows on the glass, silence drowns out the footsteps... how can I guide you today?"
            lower.contains("weather") -> 
                "Currently, a light solar breeze sweeps through our digital grid. Expect clear connections and bright insights."
            lower.contains("training") || lower.contains("schedule") -> 
                "Yes, of course. We are discussing your training program in another chat thread. Let me open it for you."
            lower.contains("hokku") || lower.contains("poem") -> 
                "Shadows on the glass,\nsilence drowns out the footsteps,\nthe night breathes light."
            lower.contains("who are you") || lower.contains("aura") -> 
                "I am Aura, a voice-driven entity designed to coexist in your ambient focus environment."
            else -> 
                "I understand your thought. The frequencies of our conversation are aligning beautifully. Tell me more."
        }
        return Pair(text, null)
    }
}
