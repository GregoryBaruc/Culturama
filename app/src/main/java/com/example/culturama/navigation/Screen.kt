package com.example.culturama.navigation

/**
 * Rotas de navegação do aplicativo.
 * Centralizadas aqui para facilitar expansão futura.
 */
sealed class Screen(val route: String) {
    object Home   : Screen("home")
    object Access : Screen("access")         // Tela provisória: Entrar / Criar conta
    object Login  : Screen("login")          // Placeholder — próxima etapa
    object SignUp : Screen("signup")         // Placeholder — próxima etapa
}
