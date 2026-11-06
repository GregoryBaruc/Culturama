package com.example.culturama.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.culturama.BuildConfig
import com.example.culturama.data.remote.ApiClient
import com.example.culturama.data.remote.ApiException
import com.example.culturama.data.remote.CadastroBody
import com.example.culturama.data.remote.SupabaseProvider
import com.example.culturama.data.remote.chamarApi
import io.github.jan.supabase.auth.SignOutScope
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * Login e sessão pelo SDK do Supabase, que guarda e renova o token sozinho.
 * O cadastro sempre passa pela API do site, que confere idade e aceite.
 */
class SupabaseAuthService private constructor() {

    /** Verdadeiro enquanto existe uma sessão ativa. */
    val isLoggedIn: Flow<Boolean> =
        SupabaseProvider.client?.auth?.sessionStatus?.map { it is SessionStatus.Authenticated }
            ?: flowOf(false)

    /**
     * Cria a conta pela API e depois entra pelo SDK. Sucesso com `false` quer dizer
     * que a conta foi criada, mas a pessoa ainda precisa entrar pela tela de login.
     */
    suspend fun register(
        name: String,
        email: String,
        password: String,
        birthDateIso: String,
        privacyAccepted: Boolean
    ): AuthResult<Boolean> {
        val cadastro = chamarApi {
            ApiClient.api.cadastro(
                CadastroBody(
                    nome = name.trim(),
                    email = email.trim().lowercase(),
                    senha = password,
                    dataNascimento = birthDateIso,
                    aceite = privacyAccepted
                )
            )
        }
        cadastro.exceptionOrNull()?.let { return AuthResult.Error(it.message.orEmpty()) }
        return AuthResult.Success(login(email, password) is AuthResult.Success)
    }

    suspend fun login(email: String, password: String): AuthResult<Unit> {
        val client = SupabaseProvider.client
            ?: return AuthResult.Error("Login indisponível: falta configurar a chave do Supabase.")
        try {
            client.auth.signInWith(Email) {
                this.email = email.trim().lowercase()
                this.password = password
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: AuthRestException) {
            return AuthResult.Error(mensagemDe(e))
        } catch (e: Exception) {
            return AuthResult.Error("Não foi possível conectar. Tente de novo.")
        }

        return conferirConta()
    }

    /**
     * Entra ou cria a conta com o Google. `AuthResult.Idle` quer dizer que
     * a pessoa fechou o seletor de contas, então não há erro para mostrar.
     * [context] precisa ser a tela atual, porque o seletor abre em cima dela.
     */
    suspend fun loginWithGoogle(context: Context): AuthResult<Unit> {
        val client = SupabaseProvider.client
            ?: return AuthResult.Error("Login indisponível: falta configurar a chave do Supabase.")
        try {
            val opcao = GetGoogleIdOption.Builder()
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setFilterByAuthorizedAccounts(false)
                .build()
            val pedido = GetCredentialRequest.Builder().addCredentialOption(opcao).build()
            val credencial = CredentialManager.create(context).getCredential(context, pedido).credential
            if (credencial !is CustomCredential ||
                credencial.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return AuthResult.Error("Não foi possível entrar com Google. Tente de novo.")
            }
            val tokenGoogle = GoogleIdTokenCredential.createFrom(credencial.data).idToken
            client.auth.signInWith(IDToken) {
                idToken = tokenGoogle
                provider = Google
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            return AuthResult.Idle
        } catch (e: NoCredentialException) {
            return AuthResult.Error("Nenhuma conta Google disponível neste aparelho.")
        } catch (e: Exception) {
            Log.w("CulturamaAuth", "Falha no login com Google", e)
            return AuthResult.Error("Não foi possível entrar com Google. Tente de novo.")
        }
        return conferirConta()
    }

    /** O Supabase não sabe de suspensão: quem aplica essa regra é o site. */
    private suspend fun conferirConta(): AuthResult<Unit> {
        val erroConta = chamarApi { ApiClient.api.eu() }.exceptionOrNull() as? ApiException
        if (erroConta?.codigo == "conta_suspensa") {
            logout()
            return AuthResult.Error(erroConta.message.orEmpty())
        }
        return AuthResult.Success(Unit)
    }

    suspend fun logout() {
        val client = SupabaseProvider.client ?: return
        try {
            client.auth.signOut(SignOutScope.LOCAL)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            client.auth.clearSession()
        }
    }

    private fun mensagemDe(e: AuthRestException): String = when (e.errorCode) {
        AuthErrorCode.InvalidCredentials -> "E-mail ou senha incorretos."
        AuthErrorCode.EmailNotConfirmed -> "Confirme seu e-mail antes de entrar."
        AuthErrorCode.OverRequestRateLimit -> "Muitas tentativas. Aguarde um pouco e tente de novo."
        else -> "Não foi possível entrar. Tente de novo."
    }

    companion object {
        val instance: SupabaseAuthService by lazy { SupabaseAuthService() }
    }
}
