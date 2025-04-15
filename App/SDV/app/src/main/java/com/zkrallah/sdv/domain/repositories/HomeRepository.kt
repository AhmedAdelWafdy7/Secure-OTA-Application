package com.zkrallah.sdv.domain.repositories

import com.zkrallah.sdv.domain.models.Message
import kotlinx.coroutines.flow.MutableStateFlow

interface HomeRepository {
    suspend fun connect(
        mqttBroker: String,
        isSecure: Boolean,
        connectionStatus: MutableStateFlow<Boolean>,
        receivedMessage: MutableStateFlow<Message?>
    )

    suspend fun subscribeToTopic(topic: String)

    suspend fun publishMessage(message: String, topic: String)

    fun disconnect(connectionStatus: MutableStateFlow<Boolean>)
}