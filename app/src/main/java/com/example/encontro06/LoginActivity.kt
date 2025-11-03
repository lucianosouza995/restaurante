package com.example.encontro06

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText

private lateinit var editTextEmail: TextInputEditText

private lateinit var editTextPassword: TextInputEditText

private lateinit var buttonLogin: Button

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)

        buttonLogin.setOnClickListener {
        performLogin()
        }

    }
    private fun performLogin(){
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,"Por favor, preencha os campos", Toast.LENGTH_SHORT).show()
            return
        }
        if (email == "admin" && password == "123") {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Usuário ou senha inválidos", Toast.LENGTH_SHORT).show()
        }
    }
}