package com.example.encontro06

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude // MUDANÇA: Importa a anotação do Firebase

// A anotação @Entity é para o Room (Banco Local)
@Entity(tableName = "Sobremesa")
data class Sobremesa (
    // As anotações @PrimaryKey e @ColumnInfo são para o Room
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_sobremesa")
    val id: Int = 0, // MUDANÇA: Adicionado "= 0"

    @ColumnInfo(name = "nome_sobremesa")
    val name: String = "", // MUDANÇA: Adicionado "= """

    @ColumnInfo(name = "preco_sobremesa")
    val price: Double = 0.0, // MUDANÇA: Adicionado "= 0.0"

    @ColumnInfo(name = "quantidade_sobremesa")
    val stockQuantity: Int = 0, // MUDANÇA: Adicionado "= 0"

    @ColumnInfo(name = "descricao_sobremesa")
    val description: String = "", // MUDANÇA: Adicionado "= """

    @ColumnInfo(name = "uri_sobremesa")
    val uri: String? = null, // MUDANÇA: Adicionado "= null"
) {
    // MUDANÇA: Adicionada anotação @get:Exclude para o Firebase
    @get:Exclude
    @Ignore // A anotação @Ignore é para o Room
    var quantity: Int = 0
}