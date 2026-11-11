package com.example.culturama.startup

/**
 * Estado de abertura do processo. Evita repetir a revelação da marca ao voltar
 * do segundo plano na mesma sessão do aplicativo.
 */
object AppStartup {
    @Volatile
    var brandRevealShownThisProcess: Boolean = false
}
