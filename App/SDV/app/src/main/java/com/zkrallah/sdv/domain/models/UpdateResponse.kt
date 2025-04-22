package com.zkrallah.sdv.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class UpdateResponse(
    val file: String,
    val version: String,
    val checksum: String,
)
