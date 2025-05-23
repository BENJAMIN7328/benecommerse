package com.example.benproject.data

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()


    fun login(email: String, password: String, onSuccess: (FirebaseUser?) -> Unit, onError: (String) -> Unit) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(task.result?.user)
                } else {
                    onError(task.exception?.localizedMessage ?: "Login failed")
                }
            }
    }
    fun register(
        email: String,
        password: String,
        onSuccess: (FirebaseUser?) -> Unit,
        onError: (String) -> Unit
    ) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess(task.result?.user)
                } else {
                    onError(task.exception?.localizedMessage ?: "Registration failed")
                }
            }
    }
}
