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

    // --- MUDANÇA 1: Adicionando Novos Listeners ---
    // Adicionamos duas novas "funções" que a SobremesaActivity
    // nos passará. São elas que serão chamadas quando o usuário
    // clicar para editar ou excluir um item.
    private val onEditClicked: (Sobremesa) -> Unit,
    private val onDeleteClicked: (Sobremesa) -> Unit

) : RecyclerView.Adapter<SobremesaAdapter.DessertViewHolder>() {

    // --- EXPLICAÇÃO ---
    // O ViewHolder é uma classe que "segura" as views de *um*
    // item da lista (um card de sobremesa).
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

        // --- MUDANÇA 2: Adicionando os Listeners de Clique no Item ---
        // O bloco `init` é executado no momento em que o ViewHolder é criado.
        // É o lugar perfeito para definir os listeners de clique
        // que valerão para o card inteiro.
        init {
            // Define o que acontece com um clique SIMPLES no card
            itemView.setOnClickListener {
                // Pega a posição do item que foi clicado
                val position = adapterPosition
                // `NO_POSITION` é uma verificação de segurança
                if (position != RecyclerView.NO_POSITION) {
                    // Chama a função onEditClicked (que recebemos lá de cima)
                    // e passa para ela a sobremesa que está nesta posição.
                    onEditClicked(desserts[position])
                }
            }

            // Define o que acontece com um clique LONGO (clicar e segurar)
            itemView.setOnLongClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Chama a função onDeleteClicked e passa a sobremesa.
                    onDeleteClicked(desserts[position])
                    // Retorna `true` para indicar que o clique foi "consumido"
                    true
                } else {
                    false
                }
            }
        }
    }

    // --- Sem Mudanças Aqui ---
    // onCreateViewHolder apenas infla o XML do item.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DessertViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.lista_item_sobremesa, parent, false)
        return DessertViewHolder(view)
    }

    // --- Sem Mudanças Aqui ---
    // getItemCount apenas diz quantos itens há na lista.
    override fun getItemCount(): Int {
        return desserts.size
    }

    // --- EXPLICAÇÃO ---
    // onBindViewHolder é chamado para CADA item da lista.
    // Ele "conecta" os dados da sobremesa (ex: "Pudim")
    // com as Views do ViewHolder (ex: o TextView do nome).
    override fun onBindViewHolder(holder: DessertViewHolder, position: Int) {
        // Pega o item de sobremesa específico para esta posição
        val dessert = desserts[position]

        // Pega as configurações de moeda (R$)
        val locale = Locale("pt", "BR")
        val currencyFormat = NumberFormat.getCurrencyInstance(locale)

        // Preenche as views com os dados da sobremesa
        holder.name.text = dessert.name
        holder.price.text = "Preço: ${currencyFormat.format(dessert.price)}"
        holder.description.text = dessert.description

        // Usa a biblioteca Glide para carregar a imagem (do celular ou da web)
        Glide.with(holder.itemView.context)
            .load(dessert.uri) // Carrega a URI (que pode ser um link da câmera ou do Firebase Storage)
            .placeholder(R.drawable.ic_launcher_background) // Imagem enquanto carrega
            .error(R.drawable.ic_launcher_foreground) // Imagem se der erro
            .into(holder.image)

        // --- Sem Mudanças Aqui (Lógica do Carrinho) ---
        // Listeners dos botões + e - para o carrinho
        holder.buttonPlus.setOnClickListener {
            dessert.quantity++
            updateItemView(holder, dessert)
            onQuantityChanged(dessert) // Avisa a SobremesaActivity sobre a mudança
        }

        holder.buttonMinus.setOnClickListener {
            if (dessert.quantity > 0) {
                dessert.quantity--
                updateItemView(holder, dessert)
                onQuantityChanged(dessert) // Avisa a SobremesaActivity sobre a mudança
            }
        }

        // Atualiza a view com a quantidade correta (ex: 0)
        updateItemView(holder, dessert)
    }

    // --- Sem Mudanças Aqui (Lógica do Carrinho) ---
    // Apenas atualiza o texto de "Quantidade" e "Total"
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