package com.example.benproject.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.benproject.data.MainViewModel
import com.example.benproject.navigation.ADD_PRODUCT
import com.example.benproject.navigation.CART
import com.example.benproject.navigation.PRODUCT_LIST

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavHostController, viewModel: MainViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFF6200EA),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome!",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF6200EA)
            )

            Spacer(modifier = Modifier.height(30.dp))

            DashboardButton(
                text = "Add Product",
                onClick = { navController.navigate(ADD_PRODUCT) }
            )

            DashboardButton(
                text = "View Products",
                onClick = { navController.navigate(PRODUCT_LIST) }
            )

            DashboardButton(
                text = "View Cart",
                onClick = { navController.navigate(CART) }
            )

            DashboardButton(
                text = "Logout",
                onClick = { viewModel.signOut(navController) },
                color = Color.Red
            )
        }
    }
}

@Composable
fun DashboardButton(text: String, onClick: () -> Unit, color: Color = Color(0xFF6200EA)) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(50.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = Color.White)
    }
}
