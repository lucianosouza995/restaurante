package com.example.encontro06

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import androidx.recyclerview.widget.RecyclerView

class SobremesaActivity : AppCompatActivity() {
private lateinit var recyclerView: RecyclerView

private lateinit var textViewTotal: TextView

private lateinit var buttonCheckout: Button

private lateinit var toolbar: Toolbar

private lateinit var dessertAdapter: SobremesaAdapter

private val dessertList = mutableListOf<Sobremesa>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sobremesa)

        recyclerView =findViewById(R.id.recyclerViewDesserts)
        textViewTotal =findViewById(R.id.textViewTotal)
        buttonCheckout =findViewById(R.id.buttonCheckout)
        toolbar =findViewById(R.id.toolbar)

        setupToolbar()
        setupRecyclerView()
        updateTotal()
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
        dessertAdapter


    }
    private fun loadDesserts() {
        dessertList.clear()

        dessertList.add(Sobremesa(1,"Pudim de Leite Condensado", "Clássico Pudim",15.0,R.drawable.pudim ))


    }
    private fun updateTotal() {

    }
}