package com.zkrallah.sdv.data.repositories

import android.util.Log
import com.zkrallah.sdv.CLIENT_ID
import com.zkrallah.sdv.domain.models.Message
import com.zkrallah.sdv.domain.models.UpdateResponse
import com.zkrallah.sdv.domain.repositories.HomeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import javax.net.ssl.SSLSocketFactory

class HomeRepositoryImpl : HomeRepository {

    private var mqttClient: MqttClient? = null

    override suspend fun connect(
        mqttBroker: String,
        isSecure: Boolean,
        connectionStatus: MutableStateFlow<Boolean>,
        receivedMessage: MutableStateFlow<Message?>
    ) {
        val brokerUrl = if (isSecure) mqttBroker.replace("mqtt://", "ssl://") else mqttBroker
        mqttClient = MqttClient(brokerUrl, CLIENT_ID, MemoryPersistence())

        val options = if (isSecure) createSecureMqttOptions() else createInsecureMqttOptions()

        mqttClient?.setCallback(createMqttCallback(connectionStatus, receivedMessage))

        mqttClient?.connect(options)
        if (mqttClient?.isConnected == true) {
            connectionStatus.value = true
            Log.d(TAG, "connect: CONNECTED TO $brokerUrl")
        } else {
            Log.e(TAG, "connect: Failed to connect")
            throw RuntimeException("Could not connect to $brokerUrl")
        }
    }

    private fun createSecureMqttOptions(): MqttConnectOptions {
        return MqttConnectOptions().apply {
            isCleanSession = true
            socketFactory = SSLSocketFactory.getDefault()
        }
    }

    private fun createInsecureMqttOptions(): MqttConnectOptions {
        return MqttConnectOptions().apply {
            isCleanSession = true
        }
    }

    private fun createMqttCallback(
        connectionStatus: MutableStateFlow<Boolean>, receivedMessage: MutableStateFlow<Message?>
    ): MqttCallback {
        return object : MqttCallback {
            override fun messageArrived(topic: String, message: MqttMessage) {
                val payloadString = String(message.payload)

                val parsedVersion = try {
                    val update = Json.decodeFromString<UpdateResponse>(payloadString)
                    Log.d(TAG, "Parsed JSON: $update")
                    update.version
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to parse payload: $payloadString", e)
                    // fallback to payload string if parsing fails
                    payloadString
                }

                val msg = Message(
                    message.id,
                    topic,
                    parsedVersion,
                    message.qos,
                    message.isDuplicate,
                    message.isRetained
                )

                receivedMessage.value = msg

                Log.d(TAG, "messageArrived: RECEIVED $parsedVersion from $topic")
            }

            override fun connectionLost(cause: Throwable?) {
                Log.d(TAG, "connectionLost: ${cause?.message}")
                connectionStatus.value = false
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {}
        }
    }

    override suspend fun subscribeToTopic(topic: String) {
        mqttClient?.let { client ->
            if (client.isConnected) {
                client.subscribe(topic)
                Log.d(TAG, "subscribeToTopic: SUBSCRIBED TO $topic")
            } else Log.e(TAG, "subscribeToTopic: Failed to subscribe to topic $topic")
        }
    }

    override suspend fun publishMessage(message: String, topic: String) {
        mqttClient?.let { client ->
            if (client.isConnected) {
                val mqttMessage = MqttMessage(message.toByteArray())
                mqttMessage.qos = 1
                client.publish(topic, mqttMessage)
                Log.d(TAG, "publishMessage: PUBLISHED TO $topic")
            } else {
                Log.e(TAG, "publishMessage: Failed to publish message $message to topic $topic")
                throw RuntimeException("Client is not connected!")
            }
        }
    }

    override fun disconnect(connectionStatus: MutableStateFlow<Boolean>) {
        mqttClient?.let { client ->
            if (client.isConnected) {
                client.disconnect()
                Log.d(TAG, "disconnect: DISCONNECTED FROM ${client.serverURI}")
                connectionStatus.value = false
            } else {
                Log.d(TAG, "disconnect: Already Disconnected")
                throw RuntimeException("Client is not connected!")
            }
        } ?: run {
            Log.e(TAG, "disconnect: Failed to disconnect from client")
            throw RuntimeException("Client is null")
        }
    }

    companion object {
        const val TAG = "HomeRepositoryImpl"
    }
}