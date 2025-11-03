package com.example.encontro06

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "Sobremesa")
data class Sobremesa (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_sobremesa")
    val id: Int = 0,

    @ColumnInfo(name = "nome_sobremesa")
    val name: String,

    @ColumnInfo(name = "preco_sobremesa")
    val price: Double,

    @ColumnInfo(name = "quantidade_sobremesa")
    val stockQuantity: Int,

    @ColumnInfo(name = "descricao_sobremesa")
    val description: String,

    @ColumnInfo(name = "uri_sobremesa")
    val uri: String?,
) {
    @Ignore
    var quantity: Int = 0
}
