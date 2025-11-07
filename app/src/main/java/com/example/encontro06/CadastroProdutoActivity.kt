package com.example.encontro06

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
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.ktx.firestore // MUDANÇA: Importa o Firestore
import com.google.firebase.ktx.Firebase // MUDANÇA: Importa o Firebase
import kotlinx.coroutines.Dispatchers // MUDANÇA: Importa Dispatchers para coroutines
import kotlinx.coroutines.launch // MUDANÇA: Importa launch
import kotlinx.coroutines.withContext // MUDANÇA: Importa withContext
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CadastroProdutoActivity : AppCompatActivity() {

    // Views da Câmera e Layout
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private lateinit var imageView: ImageView
    private lateinit var buttonTirarFoto: Button
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar

    // Views de Formulário
    private lateinit var editTextProductName: TextInputEditText
    private lateinit var editTextProductDescription: TextInputEditText
    private lateinit var editTextProductPrice: TextInputEditText
    private lateinit var editTextProductQuantity: TextInputEditText
    private lateinit var buttonRegisterProduct: Button
    // private lateinit var buttonDeleteProduct: Button // Removido, pois a exclusão está na lista

    // Variáveis de Estado
    private var imageCapture: ImageCapture? = null
    private var saveUri: Uri? = null

    // MUDANÇA: Variável para o banco de dados Firestore (nuvem)
    private val firestoreDb = Firebase.firestore

    // MUDANÇA: Variável para guardar o item em modo de edição
    // Inicia como null. Se for preenchida, estamos no "Modo Edição".
    private var sobremesaAtual: Sobremesa? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_produto)

        // Inicializa todas as views
        previewView = findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()
        editTextProductName = findViewById(R.id.editTextProductName)
        editTextProductDescription = findViewById(R.id.editTextProductDescription)
        editTextProductPrice = findViewById(R.id.editTextProductPrice)
        editTextProductQuantity = findViewById(R.id.editTextProductQuantity)
        buttonRegisterProduct = findViewById(R.id.buttonRegisterProduct)
        buttonTirarFoto = findViewById(R.id.buttonTirarFoto)
        imageView = findViewById(R.id.imageViewFoto)
        toolbar = findViewById(R.id.toolbarCadastro)

        // Configura a Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        // --- MUDANÇA: Lógica de Edição ---
        // Pega o ID passado pela SobremesaActivity
        val sobremesaId = intent.getIntExtra(SobremesaActivity.EXTRA_SOBREMESA_ID, -1)

        // Verifica se é Modo Edição (ID válido) ou Modo Cadastro (ID = -1)
        if (sobremesaId != -1) {
            // MODO EDIÇÃO
            buttonRegisterProduct.text = "Atualizar Produto"
            // Carrega os dados do Room para preencher o formulário
            loadDadosSobremesa(sobremesaId)
        } else {
            // MODO CADASTRO
            buttonRegisterProduct.text = "Cadastrar Produto"
        }

        // --- Listeners de Botões ---
        buttonRegisterProduct.setOnClickListener {
            // Esta função agora sabe lidar com "Cadastrar" e "Atualizar"
            registerProduct()
        }

        buttonTirarFoto.setOnClickListener {
            if (imageView.visibility == View.VISIBLE) {
                imageView.visibility = View.GONE
                previewView.visibility = View.VISIBLE
                saveUri = null
            } else if (imageCapture == null) {
                requestCameraPermission()
            } else {
                tirarFoto()
            }
        }
    }

    // MUDANÇA: Nova função para carregar dados do Room no Modo Edição
    private fun loadDadosSobremesa(id: Int) {
        lifecycleScope.launch {
            try {
                // Busca o item no banco local (Room) em uma thread de background
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(applicationContext)
                    sobremesaAtual = db.sobremesaDao().getById(id)
                }

                // Se o item foi encontrado, preenche a UI na thread principal
                sobremesaAtual?.let { sobremesa ->
                    editTextProductName.setText(sobremesa.name)
                    editTextProductDescription.setText(sobremesa.description)
                    editTextProductPrice.setText(sobremesa.price.toString())
                    editTextProductQuantity.setText(sobremesa.stockQuantity.toString())

                    // Carrega a imagem salva (se houver)
                    sobremesa.uri?.let { uriString ->
                        saveUri = Uri.parse(uriString)
                        imageView.setImageURI(saveUri)
                        imageView.visibility = View.VISIBLE
                        previewView.visibility = View.GONE
                        buttonTirarFoto.text = "Tirar Outra Foto"
                    }
                }
            } catch (e: Exception) {
                Log.e("CadastroProduto", "Falha ao carregar sobremesa", e)
                Toast.makeText(this@CadastroProdutoActivity, "Erro ao carregar dados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // MUDANÇA: Função de salvar agora integrada com Room e Firestore
    private fun registerProduct() {

        // ... (Sua lógica de validação de campos vazios, etc.)
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
        val price = priceStr.toDoubleOrNull()
        if (price == null) {
            editTextProductPrice.error = "Preço inválido"
            return
        }
        val quantity = quantityStr.toIntOrNull()
        if (quantity == null) {
            editTextProductQuantity.error = "Quantidade Inválida"
            return
        }
        // ... (Fim das validações)


        // Cria o objeto Sobremesa.
        // Se for um item novo (sobremesaAtual == null), o ID é 0.
        // Se for um item existente, ele usa o ID que já tem.
        val sobremesaParaSalvar = Sobremesa(
            id = sobremesaAtual?.id ?: 0,
            name = productName,
            description = description,
            price = price,
            stockQuantity = quantity,
            uri = imageUriString
        )

        // Inicia a coroutine para salvar nos bancos
        lifecycleScope.launch {
            try {
                var sobremesaFinalParaFirestore: Sobremesa

                // 1. SALVA NO BANCO LOCAL (ROOM)
                // Usamos withContext(Dispatchers.IO) para rodar em background
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(applicationContext)

                    if (sobremesaAtual == null) {
                        // MODO NOVO: Insere no Room e pega o ID recém-gerado
                        val idGeradoPeloRoom = db.sobremesaDao().insert(sobremesaParaSalvar)

                        // CORRIGE o objeto com o ID que o Room acabou de gerar
                        sobremesaFinalParaFirestore = sobremesaParaSalvar.copy(id = idGeradoPeloRoom.toInt())
                    } else {
                        // MODO UPDATE: Apenas atualiza no Room
                        db.sobremesaDao().update(sobremesaParaSalvar)
                        // O objeto já tem o ID correto
                        sobremesaFinalParaFirestore = sobremesaParaSalvar
                    }
                }

                // 2. SALVA NO BANCO NA NUVEM (FIRESTORE)
                // (Isso já roda em background, não precisa de withContext)
                firestoreDb.collection("sobremesas")
                    .document(sobremesaFinalParaFirestore.id.toString()) // Usa o ID do Room como ID do Documento
                    .set(sobremesaFinalParaFirestore) // Salva o objeto corrigido
                    .addOnSuccessListener {
                        val toastMessage = if (sobremesaAtual == null) "Produto cadastrado!" else "Produto atualizado!"
                        Toast.makeText(this@CadastroProdutoActivity, toastMessage, Toast.LENGTH_LONG).show()
                        finish() // Fecha a tela e volta para a lista
                    }
                    .addOnFailureListener { e ->
                        Log.e("CadastroProduto", "Falha ao gravar no Firestore", e)
                        Toast.makeText(this@CadastroProdutoActivity, "Erro ao salvar na nuvem: ${e.message}", Toast.LENGTH_LONG).show()
                    }

            } catch (e: Exception) {
                Log.e("CadastroProduto", "Falha ao gravar produto", e)
                Toast.makeText(this@CadastroProdutoActivity, "Erro ao gravar localmente: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }


    // --- Funções da Câmera (sem alteração) ---
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Permissão da câmera é necessária", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                startCamera()
            }
            else -> {
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

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch(exc: Exception) {
                Toast.makeText(this, "Falha ao iniciar a câmera", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun tirarFoto(){
        val imageCapture = imageCapture ?: return
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
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    saveUri = output.savedUri
                    val msg = "Foto salva com sucesso: $saveUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d("tirarFoto", msg)

                    imageView.setImageURI(saveUri)
                    imageView.visibility = View.VISIBLE
                    previewView.visibility = View.GONE
                    buttonTirarFoto.text = "Tirar Outra Foto"
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("tirarFoto", "Falha ao capturar foto: ${exc.message}", exc)
                    Toast.makeText(baseContext, "Falha ao salvar foto.", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // As funções de exclusão (handleDeleteProduct e exibirConfirmacaoExcluir)
    // foram removidas daqui, pois você as implementou corretamente na SobremesaActivity.
}