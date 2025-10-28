package com.example.encontro06

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
        loadDesserts()
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
    private fun loadDesserts() {
        dessertList.clear()

        dessertList.add(Sobremesa(1,"Pudim de Leite Condensado", "Clássico Pudim",15.00,R.drawable.pudim ))
        dessertList.add(Sobremesa(2,"Sorvete Artesanal", "2 bolas de sorvete...", 20.00, R.drawable.sorvete))
        dessertList.add(Sobremesa(3, "Mousse de Maracujá", "Leve e aerado...", 18.00, R.drawable.mousse))
        dessertList.add(Sobremesa(4, "Brigadeiro Gourmet", "Delisioso brigadeiro...", 8.00, R.drawable.brigadeiro_gourmet))
        dessertList.add(Sobremesa(5, "Bolo de Chocolate Intenso", "Fatia generosa de bolo...", 22.00, R.drawable.bolo_de_chocolate))

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