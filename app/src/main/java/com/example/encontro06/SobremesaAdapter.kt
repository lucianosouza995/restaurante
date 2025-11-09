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

// --- EXPLICAÇÃO ---
// O Adapter é a "ponte" entre a sua lista de dados (List<Sobremesa>)
// e a interface gráfica (o RecyclerView).
// Ele não sabe sobre Room ou Firestore. Ele apenas recebe uma lista
// e a exibe.

class SobremesaAdapter (
    private val desserts: List<Sobremesa>,
    private val onQuantityChanged:(Sobremesa) -> Unit,
    private val onEditClicked: (Sobremesa) -> Unit,
    private val onDeleteClicked: (Sobremesa) -> Unit

) : RecyclerView.Adapter<SobremesaAdapter.DessertViewHolder>() {

    // ... (seu inner class DessertViewHolder está correto)
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

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditClicked(desserts[position])
                }
            }
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClicked(desserts[position])
                    true
                } else {
                    false
                }
            }
        }
    }

    // ... (onCreateViewHolder e getItemCount estão corretos)
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
        // ... (configuração do 'locale' e 'currencyFormat')
        val locale = Locale("pt", "BR")
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)

        // ... (configuração de name, price, description, e Glide)
        holder.name.text = dessert.name
        holder.price.text = "Preço: ${currencyFormat.format(dessert.price)}"
        holder.description.text = dessert.description

        Glide.with(holder.itemView.context)
            .load(dessert.uri)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_foreground)
            .into(holder.image)


        // --- MUDANÇAS NA LÓGICA DO CARRINHO ---

        holder.buttonPlus.setOnClickListener {
            dessert.quantity++
            updateItemView(holder, dessert)
            // REMOVEMOS a chamada 'onQuantityChanged(dessert)' daqui.
            // O usuário agora precisa clicar em "Adicionar" para confirmar.
        }

        holder.buttonMinus.setOnClickListener {
            if (dessert.quantity > 0) {
                dessert.quantity--
                updateItemView(holder, dessert)

                // MUDANÇA: Se a quantidade chegar a zero,
                // atualizamos o carrinho imediatamente para remover o item.
                if (dessert.quantity == 0) {
                    onQuantityChanged(dessert)
                }
                updateItemView(holder, dessert)
            }
        }

        // MUDANÇA: Adicionamos o listener para o botão "Adicionar"
        holder.buttonAddToCart.setOnClickListener {
            // Agora, este botão é quem avisa a SobremesaActivity
            // para atualizar o carrinho com a quantidade selecionada.
            onQuantityChanged(dessert)

            // (Opcional) Você pode adicionar um Toast aqui para feedback
            // Toast.makeText(holder.itemView.context, "${dessert.name} adicionado!", Toast.LENGTH_SHORT).show()
        }

        updateItemView(holder, dessert)
    }

    // A lógica desta função está correta e não precisa mudar.
    // Ela habilita o botão "Adicionar" quando a quantidade é > 0.
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