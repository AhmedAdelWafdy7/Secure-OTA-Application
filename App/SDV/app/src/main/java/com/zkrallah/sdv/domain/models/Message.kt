package com.zkrallah.sdv.domain.models

data class Message(
    val id: Int,
    val topic: String,
    val payload: String,
    val qos: Int,
    val isDuplicate: Boolean,
    val isRetained: Boolean,
)