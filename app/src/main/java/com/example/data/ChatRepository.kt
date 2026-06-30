package com.example.data

import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> =
        chatDao.getMessagesForSession(sessionId)

    suspend fun createSession(session: ChatSession) {
        chatDao.insertSession(session)
    }

    suspend fun insertMessage(message: ChatMessage) {
        chatDao.insertMessage(message)
        chatDao.updateSessionLastMessage(message.sessionId, message.text, message.timestamp)
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSession(sessionId)
        chatDao.deleteMessagesForSession(sessionId)
    }
}
