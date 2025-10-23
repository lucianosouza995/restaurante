package com.example.encontro06

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale
import com.bumptech.glide.Glide
import androidx.core.view.isVisible

class SobremesaAdapter (
    private val desserts: List<Sobremesa>,
    private val onQuantityChanged:(Sobremesa) -> Unit
    ) : RecyclerView.Adapter<SobremesaAdapter.DessertViewHolder>() {

    inner class DessertViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.textViewName_2)
        val price: TextView = itemView.findViewById(R.id.textViewPrice_2)
        val description: TextView = itemView.findViewById(R.id.textViewDescription_2)
        val quantity: TextView = itemView.findViewById(R.id.textViewQuantity_2)
        val itemTotal: TextView = itemView.findViewById(R.id.textViewItemTotal_2)
        val image: ImageView = itemView.findViewById(R.id.imageViewDessert_2)
        val buttonPlus: ImageButton = itemView.findViewById(R.id.buttonPlus_2)
        val buttonMinus: ImageButton = itemView.findViewById(R.id.buttonMinus_2)
        val buttonAddToCart: Button = itemView.findViewById(R.id.buttonAddToCart_2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DessertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lista_item_sobremesa, parent, false)
        return DessertViewHolder(view)
    }

    override fun getItemCount(): Int {
        return desserts.size

    }

    override fun onBindViewHolder(holder: DessertViewHolder, position: Int) {
        val dessert = desserts[position]
        val locale = Locale("pt", "BR")
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)

        holder.name.text = dessert.name
        holder.price.text = "Preço: ${currencyFormat.format(dessert.price)}"
        holder.description.text = dessert.description

        Glide.with(holder.itemView.context)
            .load(dessert.imageId)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.image)

        holder.buttonPlus.setOnClickListener {
            dessert.quantity++
            updateItemView(holder, dessert)
            onQuantityChanged(dessert)

        }
        holder.buttonMinus.setOnClickListener {
            if (dessert.quantity > 0) {
                dessert.quantity--
                updateItemView(holder, dessert)
                onQuantityChanged(dessert)
            }
        }
        updateItemView(holder, dessert)
    }
    private fun updateItemView(holder: DessertViewHolder, dessert: Sobremesa) {
        val locale = Locale("pt", "BR")
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)
        val total = dessert.price * dessert.quantity

        holder.quantity.text = dessert.quantity.toString()
        holder.itemTotal.text = "Total: ${currencyFormat.format(total)}"
        holder.itemTotal.isVisible = dessert.quantity > 0
        holder.buttonAddToCart.isEnabled = dessert.quantity > 0

    }
}
