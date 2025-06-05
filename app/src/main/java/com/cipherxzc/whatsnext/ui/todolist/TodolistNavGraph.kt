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
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.ItemDetailViewModel
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.ItemDetailViewModelFactory
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoListViewModel
import com.cipherxzc.whatsnext.ui.todolist.viewmodel.TodoListViewModelFactory

@Composable
fun TodoListNavGraph(
    userName: String,
    todoDataViewModel: TodoDataViewModel,
    onLogout: () -> Unit
){
    val itemListViewModel: TodoListViewModel = viewModel(
        factory = TodoListViewModelFactory(todoDataViewModel)
    )
    val itemDetailViewModel: ItemDetailViewModel = viewModel(
        factory = ItemDetailViewModelFactory(todoDataViewModel)
    )
    val syncViewModel: SyncViewModel = viewModel(
        factory = SyncViewModelFactory(LocalContext.current.applicationContext as Application, todoDataViewModel)
    )

    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "itemList") {
        composable("itemList") {
            TodoListScreen(
                userName = userName,
                itemListViewModel = itemListViewModel,
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
            ItemDetailScreen(
                itemId = itemId,
                itemDetailViewModel = itemDetailViewModel,
            )
        }
    }
}