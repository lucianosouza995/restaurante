package com.example.encontro06

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

private lateinit var editTextEmail: TextInputEditText

private lateinit var editTextPassword: TextInputEditText

private lateinit var textInputLayoutConfirmPassword: TextInputLayout

private lateinit var editTextConfirmPassword: TextInputEditText

private lateinit var buttonLogin: Button

private lateinit var textViewToggleMode: TextView
private var isLoginMode = true

private lateinit var auth: FirebaseAuth

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            goToMainActivity()
        }

        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.buttonLogin)
        textViewToggleMode = findViewById(R.id.textViewToggleMode)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        textInputLayoutConfirmPassword = findViewById(R.id.textInputLayoutConfirmPassword)


        buttonLogin.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performRegistration()
            }
        }

        textViewToggleMode.setOnClickListener {
        toggleUiMode()
        }

    }
    private fun toggleUiMode(){
        if (isLoginMode){
            isLoginMode = false
            buttonLogin.text = "Cadastrar"
            textViewToggleMode.text = "Já tem uma conta? Entre"
            textInputLayoutConfirmPassword.visibility = View.VISIBLE
        }
        else{
            isLoginMode = true
            buttonLogin.text = "Entrar"
            textViewToggleMode.text ="Não tem conta? Cadastre-se"
            textInputLayoutConfirmPassword.visibility = View.GONE
        }
    }
    private fun performLogin() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha os campos", Toast.LENGTH_SHORT).show()
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login efetuado com sucesso!", Toast.LENGTH_SHORT).show()
                    goToMainActivity()
                } else {
                    Toast.makeText(
                        this,
                        "Falha no login: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        private fun performRegistration() {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmPassword = editTextConfirmPassword.text.toString().trim()

            if (password.isEmpty() || email.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha os campos", Toast.LENGTH_SHORT).show()
                return
            }
            if (password != confirmPassword) {
                editTextConfirmPassword.error = "As senhas não conferem"
                return
            }
            if (password.length < 6) {
                editTextPassword.error = "A senha deve ter pelo menos 6 caracteres"
                return
            }
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Usuário criado com sucesso!", Toast.LENGTH_SHORT).show()
                        toggleUiMode()
                    }else {
                        Toast.makeText(this, "Falha ao cadastrar", Toast.LENGTH_SHORT).show()
                    }
                }
        }
        private fun goToMainActivity() {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

