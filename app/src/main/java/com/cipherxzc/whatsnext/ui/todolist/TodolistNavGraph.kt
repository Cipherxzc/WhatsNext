package com.cipherxzc.whatsnext.ui.todolist

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cipherxzc.whatsnext.ui.core.viewmodel.SyncViewModel
import com.cipherxzc.whatsnext.ui.core.viewmodel.SyncViewModelFactory
import com.cipherxzc.whatsnext.ui.core.viewmodel.TodoDataViewModel
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoDetailViewModel
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoDetailViewModelFactory
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoListViewModel
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoListViewModelFactory

@Composable
fun TodoListNavGraph(
    userName: String,
    todoDataViewModel: TodoDataViewModel,
    onLogout: () -> Unit
){
    val todoListViewModel: TodoListViewModel = viewModel(
        factory = TodoListViewModelFactory(todoDataViewModel)
    )
    val syncViewModel: SyncViewModel = viewModel(
        factory = SyncViewModelFactory(LocalContext.current.applicationContext as Application, todoDataViewModel)
    )

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "itemList") {
        composable("itemList") {
            todoListViewModel.loadItems()
            TodoListScreen(
                userName = userName,
                todoListViewModel = todoListViewModel,
                syncViewModel = syncViewModel,
                onItemClicked = { itemId ->
                    navController.navigate("itemDetail/$itemId")
                },
                onLogout = onLogout
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