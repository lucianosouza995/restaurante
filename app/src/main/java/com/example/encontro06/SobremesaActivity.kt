package com.example.encontro06

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ListenerRegistration

class SobremesaActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var textViewTotal: TextView
    private lateinit var buttonCheckout: Button
    private lateinit var toolbar: Toolbar
    private lateinit var dessertAdapter: SobremesaAdapter

    private val dessertList = mutableListOf<Sobremesa>()
    private val cart = mutableMapOf<Int, Sobremesa>()

    // Inicializa o Firestore
    private val firestoreDb = Firebase.firestore

    // Variável para o listener, para podermos fechá-lo
    private var firestoreListener: ListenerRegistration? = null

    companion object {
        // Chave para passar o ID para a tela de edição
        const val EXTRA_SOBREMESA_ID = "SOBREMESA_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sobremesa)

        recyclerView =findViewById(R.id.recyclerViewDesserts)
        textViewTotal =findViewById(R.id.textViewTotal)
        buttonCheckout =findViewById(R.id.buttonCheckout)
        toolbar =findViewById(R.id.toolbar)

        val fabCadastrar : FloatingActionButton = findViewById(R.id.fabCadastrarSobremesa)
        fabCadastrar.setOnClickListener {
            val intent = Intent(this, CadastroProdutoActivity::class.java)
            startActivity(intent)
        }

        setupToolbar()
        setupRecyclerView()

        // O listener cuidará de carregar os dados iniciais e de qualquer
        // atualização em tempo real
        setupFirestoreListener()
    }

    override fun onResume(){
        super.onResume()
        // Não carregamos mais os dados aqui. O listener (setupFirestoreListener)
        // já cuida disso, evitando o bug da duplicação.
    }

    override fun onDestroy() {
        super.onDestroy()
        // Importante: Remove o listener para evitar vazamento de memória
        firestoreListener?.remove()
    }

    private fun setupFirestoreListener() {
        firestoreListener = firestoreDb.collection("sobremesas")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Log.e é mais correto aqui, pois é um erro de funcionalidade
                    Log.e("SobremesaActivity", "Falha ao ouvir listener.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    Log.d("SobremesaActivity", "Mudanças recebidas do Firestore: ${snapshots.size()} documentos")
                    // Há mudanças na nuvem. Sincroniza e depois recarrega a lista
                    lifecycleScope.launch {
                        sincronizarBancoLocal(snapshots.documents)
                        loadDessertsFromRoom() // Recarrega do Room após sincronizar
                    }
                }
            }
    }

    private suspend fun sincronizarBancoLocal(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        withContext(Dispatchers.IO) { // Faz a gravação em segundo plano
            val db = AppDatabase.getInstance(this@SobremesaActivity)
            for (document in documents) {
                try {
                    val sobremesa = document.toObject<Sobremesa>()
                    if (sobremesa != null) {
                        // O OnConflictStrategy.REPLACE (definido no Dao) faz a mágica:
                        // Se o ID já existe, atualiza. Se não, insere.
                        db.sobremesaDao().insert(sobremesa)
                    }
                } catch (e: Exception) {
                    Log.e("SobremesaActivity", "Falha ao converter documento", e)
                }
            }
        }
    }

    private fun loadDessertsFromRoom() {
        lifecycleScope.launch {
            // Limpa a lista da memória ANTES de recarregar do banco
            dessertList.clear()
            try {
                // Lê os dados do Room (que acabaram de ser sincronizados)
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(this@SobremesaActivity)
                    val dessertsFromDb = db.sobremesaDao().getALL()
                    dessertList.addAll(dessertsFromDb)
                }
                // Atualiza o RecyclerView
                dessertAdapter.notifyDataSetChanged()

            } catch (e: Exception) {
                Log.e("SobremesaActivity", "Erro ao carregar sobremesas do Room")
            }
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Sobremesas"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        dessertAdapter = SobremesaAdapter(
            dessertList,
            onQuantityChanged = { dessert ->
                updateCart(dessert)
            },
            onDeleteClicked = { dessert ->
                // Chama a função que mostra o pop-up de confirmação
                confirmDeleteDessert(dessert)
            },
            onEditClicked = { dessert ->
                // Chama a função para iniciar a edição
                startEditDessertActivity(dessert)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = dessertAdapter
    }

    private fun updateCart(dessert : Sobremesa){
        if (dessert.quantity > 0) {
            cart[dessert.id] = dessert
        } else {
            cart.remove(dessert.id)
        }
        updateTotal()
    }

    private fun updateTotal() {
        val total = cart.values.sumOf { it.price * it.quantity }
        val locale = Locale("pt", "BR")
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)
        textViewTotal.text = currencyFormat.format(total)
        buttonCheckout.isEnabled = total > 0
    }

    // Função para o pop-up de confirmação de exclusão
    private fun confirmDeleteDessert(dessert: Sobremesa){
        AlertDialog.Builder(this)
            .setTitle("Confirmar Exclusão")
            .setMessage("Tem certeza que deseja excluir ${dessert.name}?")
            .setPositiveButton("Excluir"){  _, _ ->
                deleteDessertFromDatabases(dessert) // Chama a função de exclusão
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // Função que deleta do Room E do Firestore
    private fun deleteDessertFromDatabases(dessert: Sobremesa) {
        lifecycleScope.launch {
            try {
                // 1. DELETA DO ROOM
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(this@SobremesaActivity)
                    db.sobremesaDao().delete(dessert)
                }

                // 2. DELETA DO FIRESTORE
                firestoreDb.collection("sobremesas")
                    .document(dessert.id.toString()) // Usa o ID do item
                    .delete()
                    .addOnSuccessListener {
                        Log.d("SobremesaActivity", "Item excluído do Firestore com sucesso")
                        Toast.makeText(this@SobremesaActivity,"${dessert.name} excluído",Toast.LENGTH_SHORT).show()
                        // O listener do Firestore (setupFirestoreListener)
                        // será acionado automaticamente e atualizará a lista.
                    }
                    .addOnFailureListener { e ->
                        Log.e("SobremesaActivity", "Erro ao excluir do Firestore", e)
                        Toast.makeText(this@SobremesaActivity, "Erro ao excluir da nuvem", Toast.LENGTH_SHORT).show()
                    }
            } catch (e: Exception) {
                Log.e("SobremesaActivity", "Erro ao excluir localmente", e)
                Toast.makeText(this@SobremesaActivity, "Erro ao excluir", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Função que abre a tela de edição
    private fun startEditDessertActivity(dessert: Sobremesa) {
        val intent = Intent(this, CadastroProdutoActivity::class.java)
        // Passa o ID do item para a tela de edição
        intent.putExtra(EXTRA_SOBREMESA_ID, dessert.id)
        startActivity(intent)
    }
}