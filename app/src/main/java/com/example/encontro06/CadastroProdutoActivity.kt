package com.example.encontro06 // Adjust package name if needed

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText // Import for TextInputEditText
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CadastroProdutoActivity : AppCompatActivity() {

    // Declare views - using lateinit for non-nullables initialized in onCreate
    private lateinit var editTextProductName: TextInputEditText
    private lateinit var editTextProductDescription: TextInputEditText
    private lateinit var editTextProductPrice: TextInputEditText
    private lateinit var editTextProductQuantity: TextInputEditText
    private lateinit var buttonRegisterProduct: Button

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var previewView: PreviewView
    private lateinit var imageView: ImageView
    private lateinit var buttonTirarFoto: Button
    private lateinit var toolbar : androidx.appcompat.widget.Toolbar

    private var imageCapture: ImageCapture? = null

    private var saveUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_produto) // Link to the XML layout

        // Initialize views using findViewById
        previewView = findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()
        editTextProductName = findViewById(R.id.editTextProductName)
        editTextProductDescription = findViewById(R.id.editTextProductDescription)
        editTextProductPrice = findViewById(R.id.editTextProductPrice)
        editTextProductQuantity = findViewById(R.id.editTextProductQuantity)
        buttonRegisterProduct = findViewById(R.id.buttonRegisterProduct)
        buttonTirarFoto = findViewById(R.id.buttonTirarFoto)
        imageView = findViewById(R.id.imageViewFoto)

        // Set action for the button click
        buttonRegisterProduct.setOnClickListener {
            registerProduct()
        }
        buttonTirarFoto.setOnClickListener {
            if (imageView.visibility == View.VISIBLE) {
                imageView.visibility = View.GONE
                previewView.visibility = View.VISIBLE
                saveUri = null
            }
            else if (imageCapture == null){
                requestCameraPermission()
            }
            else {
                    tirarFoto()

            }
        }
        toolbar = findViewById(R.id.toolbarCadastro)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)



    }
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permissão concedida. Inicie a câmera.
            startCamera()
        } else {
            // Permissão negada. Informe o usuário.
            Toast.makeText(this, "Permissão da câmera é necessária", Toast.LENGTH_SHORT).show()
        }
    }


    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                // Permissão já concedida. Inicie a câmera.
                startCamera()
            }
            else -> {
                // Permissão não concedida. Solicite ao usuário.
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            // Selecione a câmera traseira
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Desvincule todos os casos de uso
                cameraProvider.unbindAll()

                // Vincule o caso de uso de visualização à câmera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch(exc: Exception) {
                // Tratar erros
                Toast.makeText(this, "Falha ao iniciar a câmera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))

    }

    private fun tirarFoto(){
        val imageCapture = imageCapture ?: run {
            Log.e("tirarFoto","ImageCapture é nulo. A câmera foi inicializada?")
            return
        }
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
            .format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P)
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Encontro06-Imagens")
        }
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions, // A variável que você criou acima com os ContentValues que dirá onde gravar
            ContextCompat.getMainExecutor(this), // Onde o callback será executado
            object : ImageCapture.OnImageSavedCallback { // O callback para saber o resultado

                // Chamado em caso de SUCESSO
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    saveUri = output.savedUri // Guarda a URI da imagem salva
                    val msg = "Foto salva com sucesso: $saveUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d("tirarFoto", msg)

                    // ATUALIZA A UI: Mostra a foto e esconde o preview
                    imageView.setImageURI(saveUri)
                    imageView.visibility = View.VISIBLE
                    previewView.visibility = View.GONE
                    buttonTirarFoto.text = "Tirar Outra Foto"
                    }

                // Chamado em caso de ERRO
                    override fun onError(exc: ImageCaptureException) {
                        Log.e("tirarFoto", "Falha ao capturar foto: ${exc.message}", exc)
                        Toast.makeText(baseContext, "Falha ao salvar foto.", Toast.LENGTH_SHORT).show()
                    }
                }
            )
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