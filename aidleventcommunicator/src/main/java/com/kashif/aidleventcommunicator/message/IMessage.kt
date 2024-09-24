package com.kashif.aidleventcommunicator.message

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a message with content and a timestamp.
 * Implements Parcelable to allow for inter-process communication.
 *
 * @property content The content of the message.
 * @property timestamp The time the message was created, defaults to the current system time.
 */
@Parcelize
data class IMessage(
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable