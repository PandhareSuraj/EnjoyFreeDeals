package com.example.enjoyfreedeals.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

object SupabaseApi {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
    }

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun get(path: String, query: String = ""): String = client.get(restUrl(path, query)) {
        supabaseHeaders()
    }.bodyAsText()

    suspend fun post(path: String, body: JsonElement, query: String = ""): String = client.post(restUrl(path, query)) {
        supabaseHeaders()
        contentType(ContentType.Application.Json)
        setBody(body)
    }.bodyAsText()

    suspend fun patch(path: String, body: JsonElement, query: String = ""): String = client.patch(restUrl(path, query)) {
        supabaseHeaders()
        contentType(ContentType.Application.Json)
        setBody(body)
    }.bodyAsText()

    suspend fun authPost(path: String, body: JsonElement, query: String = ""): String = client.post(authUrl(path, query)) {
        supabaseHeaders()
        contentType(ContentType.Application.Json)
        setBody(body)
    }.bodyAsText()

    private fun restUrl(path: String, query: String = ""): String {
        val separator = if (query.isBlank()) "" else "?$query"
        return "${SupabaseService.SUPABASE_URL.trimEnd('/')}/rest/v1/${path.trimStart('/')}$separator"
    }

    private fun authUrl(path: String, query: String = ""): String {
        val separator = if (query.isBlank()) "" else "?$query"
        return "${SupabaseService.SUPABASE_URL.trimEnd('/')}/auth/v1/${path.trimStart('/')}$separator"
    }

    private fun io.ktor.client.request.HttpRequestBuilder.supabaseHeaders() {
        header("apikey", SupabaseService.SUPABASE_ANON_KEY)
        header("Authorization", "Bearer ${SupabaseService.SUPABASE_ANON_KEY}")
        header("Prefer", "return=representation")
    }
}
