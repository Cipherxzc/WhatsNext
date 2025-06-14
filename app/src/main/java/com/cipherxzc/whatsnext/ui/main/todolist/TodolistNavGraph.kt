package com.cipherxzc.whatsnext.ui.main.todolist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cipherxzc.whatsnext.ui.core.viewmodel.SyncViewModel
import com.cipherxzc.whatsnext.ui.core.viewmodel.TodoDataViewModel
import com.cipherxzc.whatsnext.ui.main.assistant.viewmodel.AzureViewModel
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.TodoDetailViewModel
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.TodoDetailViewModelFactory
import com.cipherxzc.whatsnext.ui.main.todolist.viewmodel.TodoListViewModel

@Composable
fun TodoListNavGraph(
    todoListTopBar: @Composable () -> Unit,
    todoDataViewModel: TodoDataViewModel,
    syncViewModel: SyncViewModel,
    todoListViewModel: TodoListViewModel,
    azureViewModel: AzureViewModel
){
    LaunchedEffect(Unit) {
        syncViewModel.sync()
        todoListViewModel.loadItems()
    }

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "itemList") {
        composable("itemList") {
            todoListViewModel.loadItems()
            TodoListScreen(
                todoListTopBar = todoListTopBar,
                todoListViewModel = todoListViewModel,
                azureViewModel = azureViewModel,
                syncViewModel = syncViewModel,
                navigateDetail = { itemId ->
                    navController.navigate("itemDetail/$itemId")
                }
            )
        }
        composable(
            "itemDetail/{itemId}",
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
            val todoDetailViewModel: TodoDetailViewModel = viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = TodoDetailViewModelFactory(todoDataViewModel, itemId)
            )

            TodoDetailScreen(
                todoDetailViewModel = todoDetailViewModel,
                onBack = {
                    todoDetailViewModel.saveItem()
                    navController.popBackStack()
                }
            )
        }
    }
}