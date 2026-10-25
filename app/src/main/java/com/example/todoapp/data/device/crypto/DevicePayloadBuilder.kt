package com.example.todoapp.data.device.crypto

import com.google.gson.stream.JsonWriter
import java.io.StringWriter

object DevicePayloadBuilder {

    fun build(
        purpose: String,
        challengeId: String,
        challenge: String,
        userId: String,
        sessionId: String,
        deviceId: String?
    ): String {
        require(
            purpose == "enrollment" ||
                    purpose == "bind" ||
                    purpose == "refresh"
        ) {
            "Unsupported device proof purpose: $purpose"
        }

        val output = StringWriter()

        JsonWriter(output).use { writer ->
            writer.beginObject()

            writer.name("version")
                .value(1)

            writer.name("purpose")
                .value(purpose)

            writer.name("challengeId")
                .value(challengeId)

            writer.name("challenge")
                .value(challenge)

            writer.name("userId")
                .value(userId)

            writer.name("sessionId")
                .value(sessionId)

            writer.name("deviceId")

            if (deviceId == null) {
                writer.nullValue()
            } else {
                writer.value(deviceId)
            }

            writer.endObject()
        }

        return output.toString()
    }
}