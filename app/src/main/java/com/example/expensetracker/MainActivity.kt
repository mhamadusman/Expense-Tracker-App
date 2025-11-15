package com.example.expensetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Filter States
enum class TransactionFilter {
    ALL, INCOME, EXPENSE
}

class MainActivity : AppCompatActivity() {

    // --- NEW CONSTANTS FOR DATA PERSISTENCE ---
    private val PREFS_NAME = "ExpenseTrackerPrefs"
    private val TRANSACTIONS_KEY = "all_transactions_json"
    // ------------------------------------------

    private lateinit var transactionAdapter: TransactionAdapter
    // **IMPORTANT CHANGE:** Initialize with loaded data right away.
    private val allTransactions = mutableListOf<Transaction>() // Stores ALL transactions

    private var currentFilter = TransactionFilter.ALL // Tracks current view filter

    private lateinit var tvBalance: TextView
    private lateinit var tvIncomeAmount: TextView
    private lateinit var tvExpenseAmount: TextView
    private lateinit var btnAddIncome: MaterialButton
    private lateinit var btnAddExpense: MaterialButton
    private lateinit var rvTransactions: RecyclerView
    private lateinit var ivFilter: ImageView // Filter Icon

    // NEW: Summary Card Views
    private lateinit var tvIncomeCount: TextView
    private lateinit var tvExpenseCount: TextView
    private lateinit var ivIncomeCurrencyIcon: ImageView
    private lateinit var ivExpenseCurrencyIcon: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadTransactions()
        initViews()
        setupRecyclerView()
        setupClickListeners()
        // Initialize UI with current data (0 or loaded data)
        updateUI()
        // Display the list based on the default filter (ALL)
        updateFilteredListAndUI()
    }



    private fun initViews() {
        tvBalance = findViewById(R.id.tvBalance)
        tvIncomeAmount = findViewById(R.id.tvIncomeAmount)
        tvExpenseAmount = findViewById(R.id.tvExpenseAmount)
        btnAddIncome = findViewById(R.id.btnAddIncome)
        btnAddExpense = findViewById(R.id.btnAddExpense)
        rvTransactions = findViewById(R.id.rvTransactions)
        ivFilter = findViewById(R.id.ivFilter)

        // NEW: Initialize Count and Icon Views from the updated XML
        tvIncomeCount = findViewById(R.id.tvIncomeCount)
        tvExpenseCount = findViewById(R.id.tvExpenseCount)

    }
    // **FEATURE 2 - SAVE DATA IN onPause()**
    /**
     * Called when the system is about to start resuming a previous activity.
     * Used to save all data to SharedPreferences to prevent data loss.
     */
    override fun onPause() {
        super.onPause()
        saveTransactions()
    }
    // ----------------------------------------------------
    // --- FEATURE 2: SAVE/LOAD FUNCTIONS IMPLEMENTATION ---
    // ----------------------------------------------------

    /**
     * CODE COMMENT: Converts the current list of transactions to a JSON string
     * using the Gson library and saves it to SharedPreferences.
     * This function is called in onPause().
     */
    private fun saveTransactions() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // 1. Use Gson to convert the mutable list into a JSON string
        val jsonString = Gson().toJson(allTransactions)

        // 2. Store the JSON string in SharedPreferences
        editor.putString(TRANSACTIONS_KEY, jsonString)
        editor.apply() // Apply asynchronously
        println("Data Persistence: Successfully saved ${allTransactions.size} transactions.")
    }

    /**
     * CODE COMMENT: Loads the JSON string from SharedPreferences, converts it
     * back into a List<Transaction> using Gson, and populates the allTransactions list.
     * This function is called in onCreate().
     */
    private fun loadTransactions() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. Retrieve the saved JSON string. Default is an empty string.
        val jsonString = prefs.getString(TRANSACTIONS_KEY, null)

        if (jsonString != null) {
            // 2. Define the target type for Gson (List<Transaction>)
            val type = object : TypeToken<MutableList<Transaction>>() {}.type

            // 3. Use Gson to convert the JSON string back to the list object
            val loadedList = Gson().fromJson<MutableList<Transaction>>(jsonString, type)

            // 4. Clear the current list and add all loaded items
            allTransactions.clear()
            allTransactions.addAll(loadedList)
            println("Data Persistence: Successfully loaded ${allTransactions.size} transactions.")
        } else {
            // No data saved yet, list remains empty (as initialized)
            println("Data Persistence: No transactions found in SharedPreferences.")
        }
    }

    private fun setupRecyclerView() {
        // NOTE: The adapter must be updated to take a 'var transactions: List<Transaction>'
        // and include an updateList(newList: List<Transaction>) function.
        transactionAdapter = TransactionAdapter(allTransactions) { transaction ->
            deleteTransaction(transaction)
        }
        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = transactionAdapter
    }

    private fun setupClickListeners() {
        btnAddIncome.setOnClickListener {
            showAddTransactionDialog(TransactionType.INCOME)
        }

        btnAddExpense.setOnClickListener {
            showAddTransactionDialog(TransactionType.EXPENSE)
        }

        // Filter Icon Click Listener
        ivFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val filterOptions = arrayOf("All Transactions", "Income Only", "Expense Only")
        val checkedItem = when (currentFilter) {
            TransactionFilter.ALL -> 0
            TransactionFilter.INCOME -> 1
            TransactionFilter.EXPENSE -> 2
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Filter Transactions")
            .setSingleChoiceItems(filterOptions, checkedItem) { dialog, which ->
                // When an item is selected, update the filter state
                currentFilter = when (which) {
                    1 -> TransactionFilter.INCOME
                    2 -> TransactionFilter.EXPENSE
                    else -> TransactionFilter.ALL
                }
                dialog.dismiss()
                updateFilteredListAndUI()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showAddTransactionDialog(type: TransactionType) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_transaction, null)

        // Get views from dialog
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)
        val tilDescription = dialogView.findViewById<TextInputLayout>(R.id.tilDescription)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)
        val tilAmount = dialogView.findViewById<TextInputLayout>(R.id.tilAmount)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etAmount)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val btnSaveTransaction = dialogView.findViewById<MaterialButton>(R.id.btnSaveTransaction)

        // Set dialog title based on type
        val title = if (type == TransactionType.INCOME) "Add Income" else "Add Expense"
        tvDialogTitle.text = title

        // Setup category spinner with filtered categories
        val categories = getFilteredCategories(type)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnSaveTransaction.setOnClickListener {
            val description = etDescription.text.toString()
            val amountStr = etAmount.text.toString()
            val category = spinnerCategory.selectedItem.toString()

            // Clear previous errors
            tilDescription.error = null
            tilAmount.error = null

            // Validate inputs
            if (description.isEmpty()) {
                tilDescription.error = "Please enter description"
                return@setOnClickListener
            }

            if (amountStr.isEmpty()) {
                tilAmount.error = "Please enter amount"
                return@setOnClickListener
            }

            val amount = amountStr.toDoubleOrNull()
            if (amount == null || amount <= 0 ) {
                tilAmount.error = "Please enter valid amount"
                return@setOnClickListener
            }

            // EXPENSE BALANCE CHECK
            if (type == TransactionType.EXPENSE) {
                val balanceText = tvBalance.text.toString()
                val currentBalance = balanceText
                    .removePrefix("Rs. ")
                    .toDoubleOrNull() ?: 0.0

                if (amount > currentBalance) {
                    tilAmount.error = "Expense (Rs. %.2f) is greater than current balance (Rs. %.2f)".format(amount, currentBalance)
                    return@setOnClickListener
                }
            }

            // Create and add transaction
            val transaction = Transaction(
                description = description,
                amount = amount,
                type = type,
                category = category
            )

            addTransaction(transaction)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getFilteredCategories(type: TransactionType): List<String> {
        return if (type == TransactionType.INCOME) {
            listOf(
                getString(R.string.category_salary),
                getString(R.string.category_freelance),
                getString(R.string.category_investment),
                getString(R.string.category_gift)
            )
        } else {
            listOf(
                getString(R.string.category_food),
                getString(R.string.category_transport),
                getString(R.string.category_shopping),
                getString(R.string.category_bills),
                getString(R.string.category_entertainment),
                getString(R.string.category_health),
                getString(R.string.category_education),
                getString(R.string.category_other)
            )
        }
    }

    private fun addTransaction(transaction: Transaction) {
        allTransactions.add(0, transaction)
        updateUI() // Update total headers (Income/Expense/Balance/Count)
        updateFilteredListAndUI() // Update the RecyclerView list
    }

    private fun deleteTransaction(transaction: Transaction) {
        val position = allTransactions.indexOf(transaction)
        if (position != -1) {
            allTransactions.removeAt(position)
            updateUI() // Update total headers
            updateFilteredListAndUI() // Update the RecyclerView list
        }
    }

    // UPDATED: Calculates totals and counts for header cards
    private fun updateUI() {
        val incomeTransactions = allTransactions.filter { it.type == TransactionType.INCOME }
        val expenseTransactions = allTransactions.filter { it.type == TransactionType.EXPENSE }

        // Calculate Sums
        val totalIncome = incomeTransactions.sumOf { it.amount }
        val totalExpense = expenseTransactions.sumOf { it.amount }
        val balance = totalIncome - totalExpense

        // Get Counts
        val incomeCount = incomeTransactions.size
        val expenseCount = expenseTransactions.size

        // Update Balance Headers
        tvBalance.text = "Rs. %.2f".format(balance)
        tvIncomeAmount.text = "Rs. %.2f".format(totalIncome)
        tvExpenseAmount.text = "Rs. %.2f".format(totalExpense)

        // NEW: Update Counts for Feature 4
        tvIncomeCount.text = "$incomeCount transactions"
        tvExpenseCount.text = "$expenseCount transactions"


    }

    // NEW: Filter transactions and update the RecyclerView
    private fun updateFilteredListAndUI() {
        val filteredList = when (currentFilter) {
            TransactionFilter.ALL -> allTransactions
            TransactionFilter.INCOME -> allTransactions.filter { it.type == TransactionType.INCOME }
            TransactionFilter.EXPENSE -> allTransactions.filter { it.type == TransactionType.EXPENSE }
        }

        // This relies on the updateList function in TransactionAdapter
        transactionAdapter.updateList(filteredList)
    }
}