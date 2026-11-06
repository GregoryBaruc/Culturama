package com.example.culturama.data.remote

import com.example.culturama.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient

/**
 * Cliente do Supabase, usado só para login. Fica null enquanto a chave pública
 * não estiver em local.properties, porque o SDK não aceita chave vazia.
 */
object SupabaseProvider {
    val client: SupabaseClient? by lazy {
        BuildConfig.SUPABASE_PUBLISHABLE_KEY.takeIf { it.isNotBlank() }?.let { chave ->
            createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = chave
            ) {
                install(Auth)
            }
        }
    }
}
