package com.cipherxzc.whatsnext.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cipherxzc.whatsnext.ui.auth.viewmodel.AuthViewModel
import androidx.compose.runtime.getValue

@Composable
fun AuthNavGraph(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    insertDefaultData: (String, (() -> Unit)?) -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "login") {
        composable("login") {
            val state by authViewModel.loginState.collectAsState()
            LoginScreen(
                onLogin = authViewModel::login,
                onNavigateToRegister = { navController.navigate("register") },
                authState = state,
                onSuccess = onLoginSuccess
            )
        }
        composable("register") {
            val state by authViewModel.registerState.collectAsState()
            RegisterScreen(
                onRegister = authViewModel::register,
                onNavigateToLogin = { navController.popBackStack() },
                authState = state,
                onSuccess = {
                    val userId = authViewModel.currentUser()?.uid
                    if (userId == null) {
                        // TODO: Handle error(根据我的理解，不可能出现这种情况)
                        return@RegisterScreen
                    }
                    insertDefaultData(userId, onLoginSuccess)
                }
            )
        }
    }
}