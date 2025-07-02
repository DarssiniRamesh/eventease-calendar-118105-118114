package com.example.calendareventfrontend.model

import java.io.Serializable
import java.util.UUID

// PUBLIC_INTERFACE
data class Event(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var description: String?,
    var startTime: Long,
    var endTime: Long,
    var reminderMinutes: Int?,
    var location: String?
) : Serializable
