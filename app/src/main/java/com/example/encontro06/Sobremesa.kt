package com.example.encontro06

data class Sobremesa (
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val imageId: Int,
    var quantity: Int = 0
)