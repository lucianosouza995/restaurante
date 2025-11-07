package com.example.encontro06

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main2)

        auth = FirebaseAuth.getInstance()

        val chipSobremesa: Chip = findViewById(R.id.chip_sobremesa)

        chipSobremesa.setOnClickListener {
            val intent = Intent(this, SobremesaActivity::class.java)

            startActivity(intent)
        }
        val buttonLogout: Button = findViewById(R.id.buttonLogout)

        buttonLogout.setOnClickListener {
            performLogout()
        }
    }
    private fun performLogout() {
        auth.signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

    }
}