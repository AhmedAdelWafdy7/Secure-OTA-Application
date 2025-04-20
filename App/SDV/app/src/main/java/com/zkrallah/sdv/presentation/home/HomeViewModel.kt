package com.zkrallah.sdv.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zkrallah.sdv.domain.models.Message
import com.zkrallah.sdv.domain.repositories.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {
    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage
    private val _receivedMessage: MutableStateFlow<Message?> = MutableStateFlow(null)
    val receivedMessage: StateFlow<Message?> = _receivedMessage
    private val _connectionStatus: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val connectionStatus: StateFlow<Boolean> = _connectionStatus
    private val _errorDialogMessage = MutableStateFlow<String?>(null)
    val errorDialogMessage: StateFlow<String?> = _errorDialogMessage

    fun connect(mqttBroker: String, isSecure: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                homeRepository.connect(mqttBroker, isSecure, _connectionStatus, _receivedMessage)
                _uiMessage.value = "Connected successfully to broker!"
                _connectionStatus.value = true
            } catch (e: Exception) {
                _uiMessage.value = "Failed to connect: ${e.message}"
                _errorDialogMessage.value = "Failed to connect: ${e.message}"
            }
        }
    }

    fun subscribeToTopic(topic: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                homeRepository.subscribeToTopic(topic)
                _uiMessage.value = "Subscribed to topic: $topic"
            } catch (e: Exception) {
                _uiMessage.value = "Subscription failed: ${e.message}"
            }
        }
    }

    fun publishMessage(message: String, topic: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                homeRepository.publishMessage(message, topic)
                _uiMessage.value = "Message published to $topic"
            } catch (e: Exception) {
                _uiMessage.value = "Failed to publish message: ${e.message}"
            }
        }
    }

    private fun disconnect() {
        try {
            homeRepository.disconnect(_connectionStatus)
            _uiMessage.value = "Disconnected successfully"
        } catch (e: Exception) {
            _uiMessage.value = "Failed to disconnect: ${e.message}"
        }
    }

    fun clearUiMessage() {
        _uiMessage.value = ""
    }

    fun clearErrorMessage() {
        _errorDialogMessage.value = null
    }

    override fun onCleared() {
        disconnect()
        super.onCleared()
    }
}