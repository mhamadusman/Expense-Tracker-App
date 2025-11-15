package com.example.expensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onDeleteClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Fixed IDs from item_transection.xml
        private val tvTransactionDescription: TextView = itemView.findViewById(R.id.tvTransactionDescription)
        private val tvTransactionCategory: TextView = itemView.findViewById(R.id.tvTransactionCategory)
        private val tvTransactionDate: TextView = itemView.findViewById(R.id.tvTransactionDate)
        private val tvTransactionAmount: TextView = itemView.findViewById(R.id.tvTransactionAmount)
        // **I'm assuming this is the delete icon based on your usage**
        private val ivDelete: ImageView = itemView.findViewById(R.id.ivTransactionIcon)
        // **I'm adding this as the category icon. Please ensure this ID exists in your layout!**
        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.ivTransactionIcon)

        fun bind(transaction: Transaction) {
            // Set description
            tvTransactionDescription.text = transaction.description

            // Set category
            tvTransactionCategory.text = transaction.category

            // **New: Set the category icon**
            ivCategoryIcon.setImageResource(getIconForCategory(transaction.category))

            // Set date
            tvTransactionDate.text = dateFormat.format(Date(transaction.timestamp))

            // Set amount with color based on type
            val amountText = "Rs. %.2f".format(transaction.amount)
            tvTransactionAmount.text = amountText

            if (transaction.type == TransactionType.INCOME) {
                tvTransactionAmount.setTextColor(
                    itemView.context.getColor(R.color.incomeGreen)
                )
            } else {
                tvTransactionAmount.setTextColor(
                    itemView.context.getColor(R.color.expenseRed)
                )
            }

            // Delete button click
           // ivDelete.setOnClickListener {
              //  onDeleteClick(transaction)
            //}

            // Long press delete
            itemView.setOnLongClickListener {
                onDeleteClick(transaction)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transection, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size

    fun updateList(newList: List<Transaction>) {
        transactions = newList
        notifyDataSetChanged() // This is the simplest way to refresh the whole list after filtering
    }

    // **Helper function to map category to icon**
    @DrawableRes
    private fun getIconForCategory(category: String): Int {
        // You will replace the placeholder drawables (e.g., R.drawable.ic_category_food)
        // with the actual names of your icons in the drawable folder.
        return when (category) {
            "Salary" -> R.drawable.ic_trending_up
            "Freelance" -> R.drawable.ic_trending_up
            "Investment" -> R.drawable.ic_trending_up
            "Gift" -> R.drawable.ic_trending_up

            // Expense Categories
            "Food" -> R.drawable.ic_food // e.g. R.drawable.ic_food
            "Transport" -> R.drawable.ic_transport
            "Shopping" -> R.drawable.ic_shopping
            "Bills" -> R.drawable.ic_bill
            "Entertainment" -> R.drawable.ic_movie
            "Health" -> R.drawable.ic_health
            "Education" -> R.drawable.ic_education

            // Default for "Other" or unknown
            else -> R.drawable.ic_education
        }
    }
}