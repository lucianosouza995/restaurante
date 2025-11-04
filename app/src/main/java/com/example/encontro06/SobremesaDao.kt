package com.example.encontro06

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SobremesaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sobremesa: Sobremesa)

    @Query("SELECT * FROM Sobremesa ORDER BY nome_sobremesa ASC")
    suspend fun getALL(): List<Sobremesa>

    @Delete
    suspend fun delete(sobremesa: Sobremesa)

}