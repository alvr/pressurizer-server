package me.alvr.pressurizer.utils

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JsonFeature

val gClient = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = GsonSerializer()
    }
}