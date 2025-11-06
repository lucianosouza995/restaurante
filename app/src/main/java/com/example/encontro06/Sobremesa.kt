package com.example.encontro06

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude

@Entity(tableName = "Sobremesa")
data class Sobremesa (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_sobremesa")
    val id: Int = 0,

    @ColumnInfo(name = "nome_sobremesa")
    val name: String = "", // Adicione = ""

    @ColumnInfo(name = "preco_sobremesa")
    val price: Double = 0.0, // Adicione = 0.0

    @ColumnInfo(name = "quantidade_sobremesa")
    val stockQuantity: Int = 0, // Adicione = 0

    @ColumnInfo(name = "descricao_sobremesa")
    val description: String = "", // Adicione = ""

    @ColumnInfo(name = "uri_sobremesa")
    val uri: String? = null, // Adicione = null
) {
    @get:Exclude // Anotação para o Firestore ignorar este campo
    @Ignore
    var quantity: Int = 0
}