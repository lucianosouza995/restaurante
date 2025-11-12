package com.example.encontro06

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import androidx.lifecycle.map // <-- MUDANÇA 1: O 'import' mudou!

class SobremesaViewModel(private val repository: SobremesaRepository) : ViewModel() {

    // --- DADOS DAS SOBREMESAS ---
    val allSobremesas: LiveData<List<Sobremesa>> = repository.allSobremesas.asLiveData()

    // --- LÓGICA DO CARRINHO ---

    private val _cart = mutableMapOf<Int, Sobremesa>()
    private val _totalDoPedido = MutableLiveData<Double>(0.0)

    // --- MUDANÇA 2: Sintaxe Moderna (sem 'Transformations.') ---
    // Em vez de 'Transformations.map(livedata, ...)'
    // Usamos 'livedata.map { ... }'
    val totalDoPedido: LiveData<String> = _totalDoPedido.map { total ->
        val locale = Locale("pt", "BR")
        NumberFormat.getCurrencyInstance(locale).format(total)
    }

    // --- MUDANÇA 3: Sintaxe Moderna (aqui também) ---
    val podeFinalizar: LiveData<Boolean> = _totalDoPedido.map { total ->
        total > 0
    }

    fun atualizarCarrinho(sobremesa: Sobremesa) {
        if (sobremesa.quantity > 0) {
            _cart[sobremesa.id] = sobremesa
        } else {
            _cart.remove(sobremesa.id)
        }
        recalcularTotal()
    }

    private fun recalcularTotal() {
        val total = _cart.values.sumOf { it.price * it.quantity }
        _totalDoPedido.value = total
    }

    // --- Funções do Repositório (sem mudança) ---
    fun insert(sobremesa: Sobremesa) = viewModelScope.launch(Dispatchers.IO) {
        repository.insert(sobremesa)
    }

    fun update(sobremesa: Sobremesa) = viewModelScope.launch(Dispatchers.IO) {
        repository.update(sobremesa)
    }

    fun delete(sobremesa: Sobremesa) = viewModelScope.launch(Dispatchers.IO) {
        _cart.remove(sobremesa.id)
        recalcularTotal()
        repository.delete(sobremesa)
    }

    fun getById(id: Int) = liveData(Dispatchers.IO) {
        emit(repository.getById(id))
    }
}

// A Factory (Fábrica) continua igual
class SobremesaViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SobremesaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            val repository = SobremesaRepository(application)
            return SobremesaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}