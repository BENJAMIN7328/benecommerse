package com.example.benproject.ui.theme.screens

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.benproject.navigation.DASHBOARD
import com.example.benproject.navigation.LOGIN
import com.example.benproject.navigation.SPLASH
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SplashScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Welcome to BenShop!", style = MaterialTheme.typography.headlineMedium)
    }

    // Delay and redirect based on auth state
    LaunchedEffect(Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
            navController.navigate(if (isLoggedIn) DASHBOARD else  LOGIN ) {
                popUpTo(SPLASH) { inclusive = true }
            }
        }, 2000)
    }
}
