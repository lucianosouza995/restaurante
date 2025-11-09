package com.example.encontro06

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// O Repositório é a única fonte de verdade.
// Ele gerencia o Room (DAO) e o Firestore.
class SobremesaRepository(context: Context) {

    // 1. Pega as instâncias do Room (local) e Firestore (nuvem)
    private val dao = AppDatabase.getInstance(context).sobremesaDao()
    private val firestoreDb = Firebase.firestore

    // 2. Expõe o Flow reativo do Room.
    // O ViewModel vai observar este Flow.
    val allSobremesas: Flow<List<Sobremesa>> = dao.getALL()

    init {
        // 3. Inicia o listener do Firestore UMA VEZ.
        // O repositório viverá enquanto o app viver,
        // então ele cuidará da sincronização.
        startFirestoreListener()
    }

    private fun startFirestoreListener() {
        firestoreDb.collection("sobremesas")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e("Repository", "Falha ao ouvir listener.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    // Quando o Firebase mudar, sincroniza o Room
                    // Usamos um CoroutineScope próprio para o Repositório
                    CoroutineScope(Dispatchers.IO).launch {
                        sincronizarBancoLocal(snapshots.documents)
                    }
                }
            }
    }

    // 4. A lógica de sincronização (movida da Activity para cá)
    private suspend fun sincronizarBancoLocal(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        for (document in documents) {
            try {
                val sobremesa = document.toObject<Sobremesa>()
                if (sobremesa != null) {
                    // O OnConflictStrategy.REPLACE faz a mágica de inserir/atualizar
                    dao.insert(sobremesa)
                }
            } catch (e: Exception) {
                Log.e("Repository", "Falha ao converter documento", e)
            }
        }
    }

    // 5. Funções de Escrita (para o ViewModel chamar)
    // Elas contêm a "dupla-ação" (salvar no Room e no Firestore)

    suspend fun insert(sobremesa: Sobremesa) {
        // 1. Salva no Room e pega o ID
        val idGerado = dao.insert(sobremesa)
        // 2. Corrige o objeto com o ID
        val sobremesaFinal = sobremesa.copy(id = idGerado.toInt())
        // 3. Salva no Firestore
        firestoreDb.collection("sobremesas")
            .document(sobremesaFinal.id.toString())
            .set(sobremesaFinal)
            .addOnFailureListener { Log.e("Repository", "Falha ao INSERIR no Firestore", it) }
    }

    suspend fun update(sobremesa: Sobremesa) {
        // 1. Salva no Room
        dao.update(sobremesa)
        // 2. Salva no Firestore
        firestoreDb.collection("sobremesas")
            .document(sobremesa.id.toString())
            .set(sobremesa)
            .addOnFailureListener { Log.e("Repository", "Falha ao ATUALIZAR no Firestore", it) }
    }

    suspend fun delete(sobremesa: Sobremesa) {
        // 1. Deleta do Room
        dao.delete(sobremesa)
        // 2. Deleta do Firestore
        firestoreDb.collection("sobremesas")
            .document(sobremesa.id.toString())
            .delete()
            .addOnFailureListener { Log.e("Repository", "Falha ao DELETAR no Firestore", it) }
    }

    suspend fun getById(id: Int): Sobremesa? {
        return dao.getById(id)
    }
}