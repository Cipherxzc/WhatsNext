package com.cipherxzc.whatsnext.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cipherxzc.whatsnext.ui.auth.AuthNavGraph
import com.cipherxzc.whatsnext.ui.auth.viewmodel.AuthViewModel
import com.cipherxzc.whatsnext.ui.core.common.ErrorScreen
import com.cipherxzc.whatsnext.ui.core.viewmodel.TodoDataViewModel
import com.cipherxzc.whatsnext.ui.main.MainScreen
import com.cipherxzc.whatsnext.ui.main.assistant.viewmodel.AzureViewModel
import com.cipherxzc.whatsnext.ui.main.assistant.viewmodel.AzureViewModelFactory

@Composable
fun WhatsNextApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val todoDataViewModel: TodoDataViewModel = viewModel()
    val azureViewModel: AzureViewModel = viewModel(
        factory = AzureViewModelFactory(todoDataViewModel)
    )

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
                    todoDataViewModel.insertDefaultData(onComplete, userId)
                }
            )
        }
        // main 模块
        composable("main") {
            val currentUser = authViewModel.currentUser()
            if (currentUser == null) {
                ErrorScreen("用户未登录")
            } else{
                todoDataViewModel.setCurrentUser(currentUser.uid)
                MainScreen(
                    userName = currentUser.displayName ?: "tourist",
                    todoDataViewModel = todoDataViewModel,
                    azureViewModel = azureViewModel,
                    onLogout = {
                        authViewModel.logout()
                        todoDataViewModel.resetCurrentUser()
                        navController.navigate("auth") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}