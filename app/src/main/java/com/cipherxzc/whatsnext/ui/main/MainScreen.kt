package com.cipherxzc.whatsnext.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cipherxzc.whatsnext.ui.core.viewmodel.TodoDataViewModel
import com.cipherxzc.whatsnext.ui.main.assistant.viewmodel.AzureViewModel
import com.cipherxzc.whatsnext.ui.main.todolist.TodoListNavGraph

private sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object TodoList : BottomNavItem("todolist", Icons.Default.List, "TodoList")
    object Report : BottomNavItem("report", Icons.Default.DateRange, "视图")
    object Assistant : BottomNavItem("assistant", Icons.Default.Person, "AI助手")
}

@Composable
fun MainScreen(
    userName: String,
    todoDataViewModel: TodoDataViewModel,
    azureViewModel: AzureViewModel,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.TodoList,
        BottomNavItem.Report,
        BottomNavItem.Assistant
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.TodoList.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.TodoList.route) {
                TodoListNavGraph(
                    userName = userName,
                    todoDataViewModel = todoDataViewModel,
                    azureViewModel = azureViewModel,
                    onLogout = onLogout
                )
            }
            composable(BottomNavItem.Report.route) {
                Text("视图（TODO）")
            }
            composable(BottomNavItem.Assistant.route) {
                Text("AI助手（TODO）")
            }
        }
    }
}