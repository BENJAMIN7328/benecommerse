package com.example.benproject.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.benproject.data.MainViewModel
import com.example.benproject.data.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavHostController, viewModel: MainViewModel) {
    val cartItems by viewModel.cart.collectAsState()
    val totalPrice = remember(cartItems) { viewModel.getTotalPrice() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Cart") }) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            // ✅ Display total price
            Text(
                text = "Total Price: $${String.format("%.2f", totalPrice)}",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(cartItems) { product ->
                    CartItem(product, viewModel)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ Pay Now Button (Triggers STK Push)
            Button(
                onClick = {
                    viewModel.initiatePayment(
                        phoneNumber = "254703816590", // Replace with user input
                        amount = totalPrice,
                        onSuccess = { println("✅ Payment successful!") },
                        onError = { errorMessage -> println("❌ Payment failed: $errorMessage") }
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text("Pay Now")
            }
        }
    }
}

@Composable
fun CartItem(product: Product, viewModel: MainViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = product.name, style = MaterialTheme.typography.titleLarge)
            Text(text = "$${String.format("%.2f", product.price)}", style = MaterialTheme.typography.bodySmall)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(onClick = { viewModel.removeFromCart(product) }) {
                    Text("Remove")
                }
            }
        }
    }
}
