package com.example.encontro06 // Adjust package name if needed

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText // Import for TextInputEditText

class CadastroProdutoActivity : AppCompatActivity() {

    // Declare views - using lateinit for non-nullables initialized in onCreate
    private lateinit var editTextProductName: TextInputEditText
    private lateinit var editTextProductDescription: TextInputEditText
    private lateinit var editTextProductPrice: TextInputEditText
    private lateinit var editTextProductQuantity: TextInputEditText
    private lateinit var buttonRegisterProduct: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_produto) // Link to the XML layout

        // Initialize views using findViewById
        editTextProductName = findViewById(R.id.editTextProductName)
        editTextProductDescription = findViewById(R.id.editTextProductDescription)
        editTextProductPrice = findViewById(R.id.editTextProductPrice)
        editTextProductQuantity = findViewById(R.id.editTextProductQuantity)
        buttonRegisterProduct = findViewById(R.id.buttonRegisterProduct)

        // Set action for the button click
        buttonRegisterProduct.setOnClickListener {
            registerProduct()
        }
    }

    private fun registerProduct() {
        // Get text from input fields
        val productName = editTextProductName.text.toString().trim()
        val description = editTextProductDescription.text.toString().trim()
        val priceStr = editTextProductPrice.text.toString().trim()
        val quantityStr = editTextProductQuantity.text.toString().trim()

        // --- Basic Validation (Optional but recommended) ---
        if (productName.isEmpty()) {
            editTextProductName.error = "Nome é obrigatório"
            return // Stop the function if validation fails
        }
        if (priceStr.isEmpty()) {
            editTextProductPrice.error = "Preço é obrigatório"
            return
        }
        if (quantityStr.isEmpty()) {
            editTextProductQuantity.error = "Quantidade é obrigatória"
            return
        }

        // --- Placeholder Action (No Database) ---
        // For now, just show a Toast message with the entered data
        val message = """
            Produto Cadastrado (Simulação):
            Nome: $productName
            Descrição: $description
            Preço: $priceStr
            Quantidade: $quantityStr
        """.trimIndent()

        Toast.makeText(this, message, Toast.LENGTH_LONG).show()

        // Here you would typically add code to save the data to a database
        // or send it to an API.

        // Optionally, clear the fields after successful "registration"
        // editTextProductName.text?.clear()
        // editTextProductDescription.text?.clear()
        // editTextProductPrice.text?.clear()
        // editTextProductQuantity.text?.clear()

        // Optionally, navigate back or to another screen
        // finish() // Closes this activity
    }
}