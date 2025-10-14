package com.example.encontro06

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.chip.Chip

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val chipSobremesa: Chip = findViewById(R.id.chip_sobremesa)

        chipSobremesa.setOnClickListener {
            val intent = Intent(this, SobremesaActivity::class.java)

            startActivity(intent)
        }
    }
}