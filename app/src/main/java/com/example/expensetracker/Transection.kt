package com.example.expensetracker

data class Transaction(
    val id: String = System.currentTimeMillis().toString(),
    val description: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransactionType {
    INCOME, EXPENSE
}