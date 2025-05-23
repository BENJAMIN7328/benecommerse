package com.example.benproject.navigation

import UpdateProductScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.benproject.data.MainViewModel
import com.example.benproject.ui.theme.screens.*



@Composable
fun AppNavGraph(navController: NavHostController) {
    val viewModel: MainViewModel = viewModel() // ✅ Initialize ViewModel

    NavHost(navController = navController, startDestination = SPLASH) {
        composable(SPLASH) { SplashScreen(navController) }
        composable(LOGIN) { LoginScreen(navController) }
        composable(REGISTER) { RegisterScreen(navController) }
        composable("dashboard") { DashboardScreen(navController, viewModel) }


        // ✅ Ensure ViewModel is passed correctly to AddProductScreen
        composable(ADD_PRODUCT) { AddProductScreen(navController, viewModel) }

        // ✅ Fix: Pass `viewModel` to `CartScreen`
        composable(CART) { CartScreen(navController, viewModel) }

        // ✅ Ensure ViewModel is passed correctly to ProductListScreen
        composable(PRODUCT_LIST) { ProductListScreen(navController, viewModel) }

        // ✅ Handle potential `null` case in UpdateProductScreen navigation
        composable("$UPDATE_PRODUCT/{product.id}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("Id") ?: ""
            val products = viewModel.products.collectAsState().value // Observe products

            val product = products.find { it.id == productId }

            if (product != null) {
                UpdateProductScreen(viewModel, product)
            } else {
                navController.navigate(PRODUCT_LIST) // Navigate back if product is not found
            }
        }

    }
    }

