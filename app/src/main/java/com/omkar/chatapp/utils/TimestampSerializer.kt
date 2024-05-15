package com.omkar.chatapp.utils

import com.google.firebase.Timestamp

class TimestampSerializer : com.google.gson.JsonSerializer<Timestamp> {
    override fun serialize(src: Timestamp?, typeOfSrc: java.lang.reflect.Type?, context: com.google.gson.JsonSerializationContext?): com.google.gson.JsonElement {
        src?.let {
            val jsonObject = com.google.gson.JsonObject()
            jsonObject.addProperty("seconds", it.seconds.toString())
            jsonObject.addProperty("nanoseconds", it.nanoseconds.toString())
            return jsonObject
        }
        return com.google.gson.JsonNull.INSTANCE
    }
}