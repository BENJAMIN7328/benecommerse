import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.benproject.data.MainViewModel
import com.example.benproject.data.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateProductScreen(viewModel: MainViewModel, product: Product) {
    val context = LocalContext.current
    val productState = remember { mutableStateOf(product.copy()) }
    val productsListState = remember { mutableStateListOf<Product>() }

    var name by remember { mutableStateOf(product.name) }
    var description by remember { mutableStateOf(product.description) }
    var price by remember { mutableStateOf(product.price.toString()) }

    LaunchedEffect(Unit) {
        viewModel.fetchProducts(productState, productsListState, context)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
        TextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
        TextField(value = price, onValueChange = { price = it }, label = { Text("Price") })

        Button(onClick = {
            val updatedProduct = mapOf(
                "name" to name,
                "description" to description,
                "price" to price.toDouble(),
                "imageUrl" to product.imageUrl
            )
            viewModel.updateProduct(product.id, updatedProduct, productState, productsListState, context)
        }) {
            Text("Update Product")
        }
    }
}
