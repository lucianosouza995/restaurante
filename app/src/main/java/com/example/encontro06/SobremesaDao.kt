package com.example.encontro06

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update // NOVO: Importar

@Dao
interface SobremesaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sobremesa: Sobremesa): Long

    @Update
    suspend fun update(sobremesa: Sobremesa)

    @Delete
    suspend fun delete(sobremesa: Sobremesa)

    @Query("SELECT * FROM Sobremesa ORDER BY nome_sobremesa ASC")
    suspend fun getALL(): List<Sobremesa>

    // NOVO: Adicione esta função
    @Query("SELECT * FROM Sobremesa WHERE id_sobremesa = :id")
    suspend fun getById(id: Int): Sobremesa
}