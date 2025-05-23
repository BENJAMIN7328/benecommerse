package com.example.benproject.data

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.benproject.network.ImgurApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val imgurService: ImgurApi = createImgurService()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products = _products.asStateFlow()

    private val _cart = MutableStateFlow<List<Product>>(emptyList())
    val cart = _cart.asStateFlow()

    fun fetchProducts(
        product: MutableState<Product>,
        products: SnapshotStateList<Product>,
        context: Context
    ): SnapshotStateList<Product> {
        val ref = FirebaseDatabase.getInstance().getReference("Products")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                products.clear()
                for (snap in snapshot.children) {
                    val value = snap.getValue(Product::class.java)
                    value?.let { products.add(it) }
                }
                if (products.isNotEmpty()) {
                    product.value = products.first()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to fetch products: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
        return products
    }

    fun updateProduct(
        productId: String,
        updatedProduct: Map<String, Any>,
        product: MutableState<Product>,
        products: SnapshotStateList<Product>,
        context: Context
    ) {
        firestore.collection("products").document(productId).update(updatedProduct)
            .addOnSuccessListener {
                viewModelScope.launch(Dispatchers.Main) { // ✅ Ensures UI update runs on the Main thread
                    fetchProducts(product, products, context) // ✅ Refresh product list after update
                }
            }
            .addOnFailureListener { error ->
                viewModelScope.launch(Dispatchers.Main) { // ✅ Ensure error handling runs on Main thread
                    Toast.makeText(context, "Update failed: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }


    fun signOut(navController: NavHostController) {
        navController.navigate("login") {
            popUpTo("login") { inclusive = true }
        }
    }

    fun uploadProductImageAndSave(
        uri: Uri,
        context: Context,
        name: String,
        description: String,
        price: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = getFileFromUri(context, uri)
            if (file == null) {
                withContext(Dispatchers.Main) { onError("Failed to process image") }
                return@launch
            }

            try {
                val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestBody)

                val response = imgurService.uploadImage("Client-ID d49f4b97e67b998", imagePart)

                if (response.isSuccessful) {
                    response.body()?.data?.link?.let { imageUrl ->
                        val product = mapOf(
                            "name" to name,
                            "description" to description,
                            "price" to price,
                            "imageUrl" to imageUrl
                        )

                        firestore.collection("products").add(product)
                            .addOnSuccessListener { viewModelScope.launch(Dispatchers.Main) { onSuccess() } }
                            .addOnFailureListener { e ->
                                viewModelScope.launch(Dispatchers.Main) { onError("Firestore error: ${e.message ?: "Unknown error"}") }
                            }
                    } ?: withContext(Dispatchers.Main) { onError("Imgur response missing image URL") }
                } else {
                    withContext(Dispatchers.Main) { onError("Imgur upload failed: ${response.message()}") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError("Exception: ${e.localizedMessage}") }
            }
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_image.jpg")
            file.outputStream().use { output -> inputStream?.copyTo(output) }
            inputStream?.close()
            file
        } catch (e: Exception) {
            println("Error processing image: ${e.localizedMessage}")
            null
        }
    }

    fun deleteProduct(productId: String) {
        firestore.collection("products").document(productId).delete()
            .addOnSuccessListener { println("Product deleted successfully!") }
            .addOnFailureListener { println("Error deleting product: ${it.message}") }
    }

    fun getTotalPrice(): Double {
        return _cart.value.sumOf { it.price }
    }

    fun removeFromCart(product: Product) {
        _cart.value = _cart.value.filterNot { it.id == product.id }
    }

    private fun fakePaymentApiCall(request: Map<String, Any>): ApiResponse {
        return ApiResponse(true, "Payment initiated successfully!")
    }

    data class ApiResponse(val isSuccessful: Boolean, val message: String)


    fun initiatePayment(
        phoneNumber: String,
        amount: Double,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val paymentRequest = mapOf(
                    "phoneNumber" to phoneNumber,
                    "amount" to amount
                )

                val response = fakePaymentApiCall(paymentRequest)

                if (response.isSuccessful) {
                    withContext(Dispatchers.Main) { onSuccess() }
                } else {
                    withContext(Dispatchers.Main) { onError("Payment failed: ${response.message}") }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError("Error: ${e.localizedMessage}") }
            }
        }
    }


    private fun createImgurService(): ImgurApi {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://api.imgur.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(ImgurApi::class.java)
    }
}
