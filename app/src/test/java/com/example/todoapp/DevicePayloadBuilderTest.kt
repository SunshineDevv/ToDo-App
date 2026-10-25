package com.example.todoapp

import com.example.todoapp.data.device.crypto.DevicePayloadBuilder
import org.junit.Assert.assertEquals
import org.junit.Test

class DevicePayloadBuilderTest {

    @Test
    fun enrollmentPayloadMatchesBackendCanonicalFormat() {
        val result = DevicePayloadBuilder.build(
            purpose = "enrollment",
            challengeId = "11111111-2222-3333-4444-555555555555",
            challenge = "abcDEF123_-challenge",
            userId = "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
            sessionId = "99999999-8888-7777-6666-555555555555",
            deviceId = null
        )

        val expected =
            """{"version":1,"purpose":"enrollment","challengeId":"11111111-2222-3333-4444-555555555555","challenge":"abcDEF123_-challenge","userId":"aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee","sessionId":"99999999-8888-7777-6666-555555555555","deviceId":null}"""

        assertEquals(expected, result)
    }
}