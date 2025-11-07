package com.btlcnpm.androidapp.data.model

// Dùng để hứng đối tượng Timestamp do Gson serialize
data class FirestoreTimestamp(
    val seconds: Long?,
    val nanoseconds: Int?
)