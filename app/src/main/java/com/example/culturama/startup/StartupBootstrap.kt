package com.example.culturama.startup

import com.example.culturama.data.auth.SupabaseAuthService
import com.example.culturama.data.remote.SupabaseProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.yield

/**
 * Inicialização leve da abertura. Só serviços essenciais —
 * não espera eventos, imagens nem módulos pesados.
 */
object StartupBootstrap {

    /**
     * @param onProgress 0f..1f — progresso real dos passos essenciais.
     */
    suspend fun prepararEssenciais(onProgress: (Float) -> Unit) {
        onProgress(0.12f)
        yield()

        runCatching { SupabaseProvider.client }
        onProgress(0.48f)
        yield()

        runCatching { SupabaseAuthService.instance.isLoggedIn.first() }
        onProgress(0.82f)
        yield()

        onProgress(1f)
    }
}
