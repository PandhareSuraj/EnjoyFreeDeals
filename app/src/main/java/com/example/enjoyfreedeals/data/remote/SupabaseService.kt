package com.example.enjoyfreedeals.data.remote

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseService {
    const val SUPABASE_URL = "https://YOUR_PROJECT.supabase.co"
    const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_PUBLISHABLE_KEY"

    val isConfigured: Boolean
        get() = SUPABASE_URL.startsWith("https://") &&
            !SUPABASE_URL.contains("YOUR_PROJECT") &&
            SUPABASE_ANON_KEY.isNotBlank() &&
            !SUPABASE_ANON_KEY.contains("YOUR_SUPABASE")

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
