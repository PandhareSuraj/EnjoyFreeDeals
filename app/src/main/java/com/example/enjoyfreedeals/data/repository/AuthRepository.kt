package com.example.enjoyfreedeals.data.repository

import android.content.Context
import com.example.enjoyfreedeals.data.mock.MockData
import com.example.enjoyfreedeals.data.model.UserProfile
import com.example.enjoyfreedeals.data.remote.SupabaseApi
import com.example.enjoyfreedeals.data.remote.SupabaseService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.URLEncoder

class AuthRepository(@Suppress("UNUSED_PARAMETER") context: Context) {
    private var mockUser: UserProfile? = MockData.user
    private var currentProfile: UserProfile? = null

    val currentUserId: String?
        get() = currentProfile?.userId ?: mockUser?.userId

    fun hasActiveSession(): Boolean = currentProfile != null

    suspend fun login(email: String, password: String): Result<UserProfile> {
        Validators.loginError(email, password)?.let { return Result.failure(IllegalArgumentException(it)) }
        if (!SupabaseService.isConfigured) return Result.success(MockData.user.copy(email = email).also { mockUser = it })
        return runCatching {
            val body = buildJsonObject {
                put("email", email)
                put("password", password)
            }
            val response = SupabaseApi.authPost("token", body, "grant_type=password")
            val userId = SupabaseApi.json.parseToJsonElement(response).jsonObject["user"]?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull
                ?: error("Your session could not be started. Please try again.")
            getUserProfile(userId).getOrThrow().also { currentProfile = it }
        }.recoverCatching {
            throw IllegalArgumentException("We could not sign you in. Please check your email and password.")
        }
    }

    suspend fun register(name: String, email: String, mobile: String, password: String, confirmPassword: String): Result<UserProfile> {
        Validators.registrationError(name, email, mobile, password, confirmPassword)?.let { return Result.failure(IllegalArgumentException(it)) }
        if (!SupabaseService.isConfigured) return Result.success(UserProfile("mock-user", name, email, mobile).also { mockUser = it })
        return runCatching {
            val body = buildJsonObject {
                put("email", email)
                put("password", password)
            }
            val response = SupabaseApi.authPost("signup", body)
            val userId = SupabaseApi.json.parseToJsonElement(response).jsonObject["user"]?.jsonObject?.get("id")?.jsonPrimitive?.contentOrNull
                ?: error("Your account could not be created. Please try again.")
            val profile = UserProfile(userId = userId, name = name, email = email, mobile = mobile)
            SupabaseApi.post("users", SupabaseApi.json.parseToJsonElement(SupabaseApi.json.encodeToString(profile)))
            currentProfile = profile
            profile
        }.recoverCatching {
            throw IllegalArgumentException("We could not create your account right now. Please try again.")
        }
    }

    suspend fun getUserProfile(userId: String = currentUserId ?: ""): Result<UserProfile> = runCatching {
        if (!SupabaseService.isConfigured || userId.isBlank()) return@runCatching currentProfile ?: mockUser ?: MockData.user
        val response = SupabaseApi.get("users", "user_id=eq.${encode(userId)}&limit=1")
        SupabaseApi.json.decodeFromString<List<UserProfile>>(response).firstOrNull() ?: currentProfile ?: mockUser ?: MockData.user
    }

    suspend fun updateSavedDeals(dealIds: List<String>): Result<Unit> = runCatching {
        val updated = (currentProfile ?: mockUser ?: MockData.user).copy(savedDeals = dealIds)
        currentProfile = updated
        mockUser = updated
        if (SupabaseService.isConfigured && updated.userId.isNotBlank()) {
            SupabaseApi.patch("users", jsonArrayPatch("saved_deals", dealIds), "user_id=eq.${encode(updated.userId)}")
        }
    }

    suspend fun updateClickedDeals(dealIds: List<String>): Result<Unit> = runCatching {
        val updated = (currentProfile ?: mockUser ?: MockData.user).copy(clickedDeals = dealIds)
        currentProfile = updated
        mockUser = updated
        if (SupabaseService.isConfigured && updated.userId.isNotBlank()) {
            SupabaseApi.patch("users", jsonArrayPatch("clicked_deals", dealIds), "user_id=eq.${encode(updated.userId)}")
        }
    }

    fun logout() {
        currentProfile = null
        mockUser = MockData.user
    }

    private fun jsonArrayPatch(key: String, values: List<String>) = buildJsonObject {
        put(key, buildJsonArray { values.forEach { add(JsonPrimitive(it)) } })
    }

    private fun encode(value: String): String = URLEncoder.encode(value, Charsets.UTF_8.name())
}
