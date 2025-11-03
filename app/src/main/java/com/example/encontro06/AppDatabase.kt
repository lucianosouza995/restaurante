package com.example.encontro06

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Sobremesa :: class], version = 1, exportSchema = false)

abstract class AppDatabase : RoomDatabase(){
    abstract fun sobremesaDao(): SobremesaDao
    companion object {

        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "restaurante_database"
                )
                    .build()
                    INSTANCE = instance
                    instance
            }
        }
    }
}