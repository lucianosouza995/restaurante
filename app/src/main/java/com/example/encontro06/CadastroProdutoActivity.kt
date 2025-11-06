package com.example.encontro06 // Adjust package name if needed

import android.Manifest
import android.R.attr.id
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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private val firestoreDb = Firebase.firestore

    private var sobremesaAtual: Sobremesa? = null

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

        toolbar.setNavigationOnClickListener {
            finish()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val sobremesaId = intent.getIntExtra("SOBREMESA", -1)
        if (sobremesaId) {
            loadDataSobremesa(sobremesaId)
        }
    }
    private fun loadDataSobremesa(){
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(applicationContext)
                    sobremesaAtual = db.sobremesaDao().getById(id)
                }

            }catch (e: Exception){

            }
        }
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

        val productName = editTextProductName.text.toString().trim()
        val description = editTextProductDescription.text.toString().trim()
        val priceStr = editTextProductPrice.text.toString().trim()
        val quantityStr = editTextProductQuantity.text.toString().trim()
        val imageUriString = saveUri?.toString()

        if (productName.isEmpty()) {
            editTextProductName.error = "Nome é obrigatório"
            return
        }
        if (priceStr.isEmpty()) {
            editTextProductPrice.error = "Preço é obrigatório"
            return
        }
        if (quantityStr.isEmpty()) {
            editTextProductQuantity.error = "Quantidade é obrigatória"
            return
        }

        val price = priceStr.toDoubleOrNull()

        val quantity = quantityStr.toIntOrNull()

        if (price == null) {
            editTextProductPrice.error = "Preço inválido"
            return
        }
        if (quantity == null) {
            editTextProductQuantity.error = "Quantidade Inválida"
            return
        }

        val sobremesaParaSalvar = Sobremesa(
            id = sobremesaAtual?.id ?: 0,
            name = productName,
            description = description,
            price = price,
            stockQuantity = quantity,
            uri = imageUriString
        )
        lifecycleScope.launch {
            try {
                var sobremesaFinalParafirestore: Sobremesa
                withContext(Dispatchers.IO){
                    val db = AppDatabase.getInstance(applicationContext)
                    if(sobremesaAtual == null){
                        val idRoom = db.sobremesaDao().insert(sobremesaParaSalvar)
                        sobremesaFinalParafirestore = sobremesaParaSalvar.copy(id = idRoom.toInt)
                    }else{
                        db.sobremesaDao().update(sobremesaParaSalvar)
                        sobremesaFinalParafirestore = sobremesaParaSalvar

                    }
                }
                firestoreDb.collection("sobremesas")
                    .document(sobremesaFinalParafirestore.id.toString())
                    .set(sobremesaFinalParafirestore)
                    .addOnSuccessListener {
                        val toastMessage = if(sobremesaAtual == null)"Produto Cadastrado!"
                        else "Produto atualizado!"
                        Toast.makeText(this@CadastroProdutoActivity, toastMessage, Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.e("CadastroProduto", "Falha ao gravar no Firestore", e)
                        Toast.makeText(this@CadastroProdutoActivity, "Erro ao salvar na nuvem",
                            Toast.LENGTH_SHORT).show()
                    }
                }catch (e: Exception) {
                Log.e("CadastroProduto", "Falha ao gravar produto", e)
                Toast.makeText(this@CadastroProdutoActivity, "Erro ao gravar local", Toast.LENGTH_SHORT).show()
                }
        }

    }
}