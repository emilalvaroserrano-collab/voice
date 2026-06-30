package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.AuraAudioHelper
import com.example.api.Content
import com.example.api.GeminiApiClient
import com.example.api.Part
import com.example.auth.AuthManager
import com.example.auth.AuraUser
import com.example.data.AppDatabase
import com.example.data.ChatMessage
import com.example.data.ChatRepository
import com.example.data.ChatSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class VoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

class AuraViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    
    // Auth
    val authManager = AuthManager(context)
    val currentUser: StateFlow<AuraUser?> = authManager.currentUser
    
    // Database and Repo
    private val database = AppDatabase.getDatabase(context)
    private val repository = ChatRepository(database.chatDao())
    
    // UI Flows
    val sessions: StateFlow<List<ChatSession>> = repository.allSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId
    
    val currentMessages: StateFlow<List<ChatMessage>> = _currentSessionId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getMessagesForSession(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Voice Chat State
    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState

    // Transcript text of what user/Aura says in voice screen
    private val _voiceTranscript = MutableStateFlow("Tap the mic below to begin speaking with Aura.")
    val voiceTranscript: StateFlow<String> = _voiceTranscript

    private val _voiceSubtext = MutableStateFlow("Ready")
    val voiceSubtext: StateFlow<String> = _voiceSubtext

    // Voice & Model Configuration
    private val _selectedVoice = MutableStateFlow("Kore") // Default prebuilt voice name
    val selectedVoice: StateFlow<String> = _selectedVoice

    private val _selectedModel = MutableStateFlow("Aura 1.2 BETA") // Matches top bar dropdown
    val selectedModel: StateFlow<String> = _selectedModel

    // Mute/Mute State for Speak Responses
    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted

    init {
        // Automatically create a default session if sessions are empty
        viewModelScope.launch {
            sessions.collect { list ->
                if (list.isEmpty() && currentUser.value != null) {
                    val defaultSession = ChatSession(
                        id = "aura_default",
                        title = "General Conversation",
                        lastMessage = "Welcome. Tap Voice Chat or type a message.",
                        timestamp = System.currentTimeMillis()
                    )
                    repository.createSession(defaultSession)
                    _currentSessionId.value = "aura_default"
                } else if (_currentSessionId.value == null && list.isNotEmpty()) {
                    _currentSessionId.value = list.first().id
                }
            }
        }
        
        // Listen to Auth State Changes to Auto-Initialize Database Sessions
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null && _currentSessionId.value == null) {
                    val defaultSession = ChatSession(
                        id = "aura_default",
                        title = "General Conversation",
                        lastMessage = "Welcome. Tap Voice Chat or type a message.",
                        timestamp = System.currentTimeMillis()
                    )
                    repository.createSession(defaultSession)
                    _currentSessionId.value = "aura_default"
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun createNewSession(title: String) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            val session = ChatSession(id = id, title = title, lastMessage = "New Conversation created.")
            repository.createSession(session)
            _currentSessionId.value = id
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = sessions.value.firstOrNull()?.id
            }
        }
    }

    fun setVoice(voiceName: String) {
        _selectedVoice.value = voiceName
    }

    fun setModel(modelName: String) {
        _selectedModel.value = modelName
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            AuraAudioHelper.stopPlayback()
            if (_voiceState.value == VoiceState.SPEAKING) {
                _voiceState.value = VoiceState.IDLE
            }
        }
    }

    /**
     * Send a traditional text message in the chat
     */
    fun sendTextMessage(text: String) {
        val sessionId = _currentSessionId.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            // 1. Insert User Message
            val userMsg = ChatMessage(sessionId = sessionId, sender = "user", text = text)
            repository.insertMessage(userMsg)

            // 2. Fetch Gemini Response
            _voiceState.value = VoiceState.THINKING
            
            // Build conversation history for context
            val history = currentMessages.value.takeLast(10).map { msg ->
                Content(
                    role = if (msg.sender == "user") "user" else "model",
                    parts = listOf(Part(text = msg.text))
                )
            }

            val result = withContext(Dispatchers.IO) {
                GeminiApiClient.askGemini(
                    prompt = text,
                    voiceName = _selectedVoice.value,
                    history = history
                )
            }

            // 3. Insert Aura Response
            val auraMsg = ChatMessage(
                sessionId = sessionId,
                sender = "aura",
                text = result.first,
                audioBase64 = result.second
            )
            repository.insertMessage(auraMsg)
            _voiceState.value = VoiceState.IDLE

            // Play voice reply automatically if not muted and voice bytes are present
            if (!_isMuted.value && result.second != null) {
                playAudioResponse(result.second!!)
            }
        }
    }

    /**
     * Trigger Mic Recording
     */
    fun startVoiceRecording() {
        _voiceState.value = VoiceState.LISTENING
        _voiceTranscript.value = "Listening..."
        _voiceSubtext.value = "Aura is capturing your frequencies..."
        
        val success = AuraAudioHelper.startRecording(context)
        if (!success) {
            // Simulated or Permission Denied fallback
            _voiceTranscript.value = "Tap or Speak: Shadows on the glass..."
            _voiceSubtext.value = "Mic not configured. Tap again to send simulated speech!"
        }
    }

    /**
     * Stop Mic Recording and send to Gemini
     */
    fun stopVoiceRecording(simulatedText: String? = null) {
        _voiceState.value = VoiceState.THINKING
        _voiceTranscript.value = "Aura is thinking..."
        _voiceSubtext.value = "Processing audio stream..."

        viewModelScope.launch {
            val audioBase64 = withContext(Dispatchers.IO) {
                AuraAudioHelper.stopRecordingAndGetBase64()
            }

            // If base64 is null and no simulated text, use default
            val prompt = simulatedText ?: if (audioBase64 == null) "What do you think of my hokku?" else ""
            
            // 1. Add user message to local chat persistence
            val sessionId = _currentSessionId.value ?: "aura_default"
            val userMsgText = if (simulatedText != null) simulatedText else "Voice Query (${if (audioBase64 != null) "Audio Capture" else "Hokku Theme"})"
            repository.insertMessage(ChatMessage(sessionId = sessionId, sender = "user", text = userMsgText))

            // 2. Query Gemini
            val result = withContext(Dispatchers.IO) {
                GeminiApiClient.askGemini(
                    prompt = prompt,
                    voiceName = _selectedVoice.value,
                    userAudioBase64 = audioBase64
                )
            }

            // 3. Add Aura's response to local chat persistence
            val auraMsg = ChatMessage(
                sessionId = sessionId,
                sender = "aura",
                text = result.first,
                audioBase64 = result.second
            )
            repository.insertMessage(auraMsg)

            // Update UI transcript with Gemini's text response
            _voiceTranscript.value = result.first
            _voiceSubtext.value = if (simulatedText != null) "Text Input Reply" else "Voice Chat Audio Streamed"

            // 4. Play audio response if present
            if (!_isMuted.value && result.second != null) {
                _voiceState.value = VoiceState.SPEAKING
                playAudioResponse(result.second!!)
            } else {
                _voiceState.value = VoiceState.IDLE
            }
        }
    }

    private fun playAudioResponse(base64Data: String) {
        _voiceState.value = VoiceState.SPEAKING
        AuraAudioHelper.playAudioFromBase64(context, base64Data) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun stopPlayback() {
        AuraAudioHelper.stopPlayback()
        if (_voiceState.value == VoiceState.SPEAKING) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    override fun onCleared() {
        super.onCleared()
        AuraAudioHelper.stopPlayback()
    }
}
