package com.cipherxzc.whatsnext.ui.main

import android.app.Application
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cipherxzc.whatsnext.ui.core.viewmodel.SyncViewModel
import com.cipherxzc.whatsnext.ui.core.viewmodel.SyncViewModelFactory
import com.cipherxzc.whatsnext.ui.core.viewmodel.TodoDataViewModel
import com.cipherxzc.whatsnext.ui.main.analytics.AnalyticsScreen
import com.cipherxzc.whatsnext.ui.main.assistant.AssistantScreen
import com.cipherxzc.whatsnext.ui.main.assistant.viewmodel.AssistantViewModel
import com.cipherxzc.whatsnext.ui.main.assistant.viewmodel.AzureViewModel
import com.cipherxzc.whatsnext.ui.main.todolist.TodoListNavGraph
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.TodoListViewModel
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.TodoListViewModelFactory

private sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object TodoList : BottomNavItem("todolist", Icons.AutoMirrored.Filled.List, "TodoList")
    object Report : BottomNavItem("report", Icons.Default.DateRange, "视图")
    object Assistant : BottomNavItem("assistant", Icons.Default.Person, "AI助手")
}

@OptIn(ExperimentalMaterial3Api::class)
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

    val syncViewModel: SyncViewModel = viewModel(
        factory = SyncViewModelFactory(LocalContext.current.applicationContext as Application, todoDataViewModel)
    )
    val todoListViewModel: TodoListViewModel = viewModel(
        factory = TodoListViewModelFactory(todoDataViewModel)
    )
    val assistantViewModel: AssistantViewModel = viewModel(
        factory = AssistantViewModel.factory(azureViewModel, todoDataViewModel)
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (currentRoute == item.route) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute != item.route) {
                                if (item.route == "todolist") {
                                    syncViewModel.sync()
                                }
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        alwaysShowLabel = false
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
                    todoListTopBar = { TodoListTopBar(userName, onLogout) },
                    todoDataViewModel = todoDataViewModel,
                    syncViewModel = syncViewModel,
                    todoListViewModel = todoListViewModel,
                    azureViewModel = azureViewModel
                )
            }
            composable(BottomNavItem.Report.route) {
                val todoItems = todoListViewModel.todoItemsFlow.collectAsStateWithLifecycle().value
                val overdueItems = todoListViewModel.overdueItemsFlow.collectAsStateWithLifecycle().value
                AnalyticsScreen(
                    todoItems = todoItems + overdueItems
                )
            }
            composable(BottomNavItem.Assistant.route) {
                assistantViewModel.initAssistant()
                AssistantScreen(assistantViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListTopBar(
    userName: String,
    onLogout: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        actions = {
            IconButton(
                onClick = onLogout,
                modifier = Modifier.animateContentSize()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
        )
    )
}