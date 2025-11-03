package com.example.encontro06

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
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

class SobremesaActivity : AppCompatActivity() {
private lateinit var recyclerView: RecyclerView

private lateinit var textViewTotal: TextView

private lateinit var buttonCheckout: Button

private lateinit var toolbar: Toolbar

private lateinit var dessertAdapter: SobremesaAdapter

private val dessertList = mutableListOf<Sobremesa>()

    private val cart = mutableMapOf<Int, Sobremesa>()

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

        }
    override fun onResume(){
        super.onResume()
        lifecycleScope.launch {
            loadDesserts()
            dessertAdapter.notifyDataSetChanged()
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
        dessertAdapter = SobremesaAdapter(dessertList) { dessert ->
            updateCart(dessert)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = dessertAdapter
    }
    private suspend fun loadDesserts() {
        dessertList.clear()
        try {
            val dessertsFromDb: List<Sobremesa>
            withContext(Dispatchers.IO) {
                val db = AppDatabase.getInstance(this@SobremesaActivity)
                dessertsFromDb = db.sobremesaDao().getALL()
            }
            dessertList.addAll(dessertsFromDb)

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
}