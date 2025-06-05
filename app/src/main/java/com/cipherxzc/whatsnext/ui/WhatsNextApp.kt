package com.cipherxzc.whatsnext.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cipherxzc.whatsnext.ui.viewmodel.AuthViewModel

@Composable
fun WhatsNextApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val databaseViewModel: DatabaseViewModel = viewModel()

    val startRoute = if (authViewModel.currentUser() != null) "main" else "auth"

    NavHost(navController, startDestination = startRoute) {
        // auth 模块
        composable("auth") {
            AuthNavGraph(
                authViewModel=authViewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("auth") { inclusive = true }
                    }
                },
                insertDefaultData = { userId, onComplete ->
                    databaseViewModel.insertDefaultData(userId, onComplete)
                }
            )
        }
        // main 模块
        composable("main") {
            val currentUser = authViewModel.currentUser()
            if (currentUser == null) {
                ErrorScreen()
            } else{
                databaseViewModel.setCurrentUser(currentUser.uid)
                MainNavGraph(
                    userName = currentUser.displayName ?: "tourist",
                    databaseViewModel = databaseViewModel,
                    onLogout = {
                        authViewModel.logout()
                        databaseViewModel.resetCurrentUser()
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}