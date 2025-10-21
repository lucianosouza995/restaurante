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

    }

    private fun updateTotal() {

    }
}