package com.example.encontro06

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // Importe viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer // Importe Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
// REMOVA todos os imports do Firestore, Coroutines, etc.

class SobremesaActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var textViewTotal: TextView
    private lateinit var buttonCheckout: Button
    private lateinit var toolbar: Toolbar
    private lateinit var dessertAdapter: SobremesaAdapter

    // --- MUDANÇA 1: REMOVER O ESTADO LOCAL ---
    // A lista de sobremesas agora é vazia. O ViewModel vai preenchê-la.
    private val dessertList = mutableListOf<Sobremesa>()
    // REMOVA: private val cart = mutableMapOf<Int, Sobremesa>()

    // Obter o ViewModel
    private val sobremesaViewModel: SobremesaViewModel by viewModels {
        SobremesaViewModelFactory(application)
    }

    companion object {
        const val EXTRA_SOBREMESA_ID = "SOBREMESA_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sobremesa)

        // Inicialização de Views (igual)
        recyclerView = findViewById(R.id.recyclerViewDesserts)
        textViewTotal = findViewById(R.id.textViewTotal)
        buttonCheckout = findViewById(R.id.buttonCheckout)
        toolbar = findViewById(R.id.toolbar)
        // ... (fabCadastrar)

        setupToolbar()
        // O setupRecyclerView agora passa as funções do ViewModel
        setupRecyclerView()

        // --- MUDANÇA 2: OBSERVAR O VIEWMODEL ---

        // Observador 1: Atualiza a LISTA do RecyclerView
        sobremesaViewModel.allSobremesas.observe(this, Observer { sobremesas ->
            dessertList.clear()
            dessertList.addAll(sobremesas)
            dessertAdapter.notifyDataSetChanged()
        })

        // Observador 2: Atualiza o TEXTO do total
        sobremesaViewModel.totalDoPedido.observe(this, Observer { totalFormatado ->
            textViewTotal.text = totalFormatado
        })

        // Observador 3: Atualiza o BOTÃO de finalizar
        sobremesaViewModel.podeFinalizar.observe(this, Observer { podeFinalizar ->
            buttonCheckout.isEnabled = podeFinalizar
        })

        // REMOVA: setupFirestoreListener() e todo o resto da lógica de dados
    }

    // REMOVA: onResume(), onDestroy(), setupFirestoreListener(),
    // sincronizarBancoLocal(), loadDessertsFromRoom()

    private fun setupToolbar() {
        // ... (código igual)
    }

    private fun setupRecyclerView() {
        dessertAdapter = SobremesaAdapter(
            dessertList, // Passa a lista (que será atualizada pelo Observador 1)

            // --- MUDANÇA 3: USAR O VIEWMODEL NOS CALLBACKS ---
            onQuantityChanged = { dessert ->
                // Pede ao ViewModel para atualizar o carrinho
                sobremesaViewModel.atualizarCarrinho(dessert)
            },
            onDeleteClicked = { dessert ->
                // Pede ao ViewModel para deletar
                confirmDeleteDessert(dessert)
            },
            onEditClicked = { dessert ->
                startEditDessertActivity(dessert)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = dessertAdapter
    }

    // REMOVA: updateCart(dessert : Sobremesa)
    // REMOVA: updateTotal()

    private fun confirmDeleteDessert(dessert: Sobremesa){
        AlertDialog.Builder(this)
            // ... (código igual)
            .setPositiveButton("Excluir"){  _, _ ->
                // A Activity "pede" para o ViewModel
                sobremesaViewModel.delete(dessert)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // REMOVA: deleteDessertfromDatabase(dessert: Sobremesa)

    private fun startEditDessertActivity(dessert: Sobremesa) {
        // ... (código igual)
    }
}