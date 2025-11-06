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
import com.google.firebase.Firebase
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class SobremesaActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView

    private lateinit var textViewTotal: TextView

    private lateinit var buttonCheckout: Button

    private lateinit var toolbar: Toolbar

    private lateinit var dessertAdapter: SobremesaAdapter

    private val dessertList = mutableListOf<Sobremesa>()

    private val cart = mutableMapOf<Int, Sobremesa>()

    private val db = Firebase.firestore

    private var firestoreListenner: ListenerRegistration? = null

    private val firestoreDb = Firebase.firestore

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

        setupFirestoreListenner()

    }
    override fun onResume(){
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
        firestoreListenner?.remove()
    }
    private fun  setupFirestoreListenner(){
        firestoreListenner = firestoreDb.collection("sobremesas")
            .addSnapshotListener {  snapshots, e ->
                if (e !=null){
                    Log.e("SobremesaActivity", "Falha no listenner", e)
                    return@addSnapshotListener
                }
                if(snapshots != null){
                    Log.e("SobremesaActivity", "Mudanças recebidas do Firestore")
                    lifecycleScope.launch {
                        syncDblocal(snapshots.documents)
                        loadDesserts()
                    }
                }
            }
    }
    private suspend fun syncDblocal(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(this@SobremesaActivity)
            for (document in documents) {
                try {
                    val sobremesa = document.toObject<Sobremesa>()
                    if (sobremesa != null) {
                        db.sobremesaDao().insert(sobremesa)
                    }

                }catch (e: Exception){
                    Log.e("SobremesaActivity", "Falha ao converter documento")
                }
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
        dessertAdapter = SobremesaAdapter(dessertList, onQuantityChanged = { dessert ->
            updateCart(dessert)},

            onDeleteClicked = { dessert ->
                confirmDeleteDessert(dessert)
            },
            onEditClicked = { dessert ->
                // NOVO: Chama a função para iniciar a edição
                startEditDessertActivity(dessert)
            }

        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = dessertAdapter
    }
    private suspend fun loadDesserts() {
        dessertList.clear()
        try {
            db.collection("Sobremesa")
                .get()
                .addOnSuccessListener { result ->
                    for (document in result) {
                        val sobremesa = document.toObject(Sobremesa::class.java)

                        dessertList.add(sobremesa)
                    }
                    dessertAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("SobremesaActivity", "Erro ao carregar sobremesa", e)
                }


        } catch (e: Exception){
            Log.e("SobremesaActivity", "Erro ao carregar sobremesas ")
        }

    }
    private fun updateCart(dessert : Sobremesa){
        if (dessert.quantity > 0) {
            cart[dessert.id] = dessert

        }else {
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
    private fun confirmDeleteDessert(dessert: Sobremesa){
        AlertDialog.Builder(this)
            .setTitle("Conformar Exclusão")
            .setMessage("Tem certeza que deseja excluir ${dessert.name}")
            .setPositiveButton("Excluir"){  _, _ ->
                deleteDessertfromDatabase(dessert)
            }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun deleteDessertfromDatabase(dessert: Sobremesa) {
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getInstance(this@SobremesaActivity)
                    db.sobremesaDao().delete(dessert)
                }
                val position = dessertList.indexOf(dessert)
                if (position != -1) {
                    dessertList.removeAt(position)
                    dessertAdapter.notifyItemRemoved(position)
                }
                dessert.quantity = 0
                updateCart(dessert)

                Toast.makeText(this@SobremesaActivity,"${dessert.name} excluído",Toast.LENGTH_SHORT).show()


            } catch (e: Exception) {
                Log.e("SobremesaActivity", "Erro ao excluir", e)
                Toast.makeText(this@SobremesaActivity, "Erro ao excluir", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun startEditDessertActivity(dessert: Sobremesa) {
        val intent = Intent(this, CadastroProdutoActivity::class.java)
        // A "chave" para o modo de edição é passar o ID do item
        intent.putExtra("SOBREMESA_ID", dessert.id)
        startActivity(intent)
    }
}