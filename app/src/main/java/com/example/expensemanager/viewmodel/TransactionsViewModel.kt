package com.example.expensemanager.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensemanager.data.AppDatabase
import com.example.expensemanager.data.TransactionEntity
import com.example.expensemanager.repo.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = TransactionRepository(AppDatabase.getInstance(application).transactionDao())

    // raw transactions as stateful hot flow
    val transactions: StateFlow<List<TransactionEntity>> = repo.streamAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // grouped by date (desc) for UI convenience
    val groupedByDate: StateFlow<Map<String, List<TransactionEntity>>> =
        transactions.map { list ->
            list.sortedByDescending { it.date }.groupBy { it.date }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    // last scan timestamp (keeps re-scan incremental); persisted outside VM in real app
    private var lastScanMillis: Long? = null

    fun scanNow() {
        viewModelScope.launch {
            try {
                val inserted = repo.scanAndSave(getApplication(), lastScanMillis)
                if (inserted > 0) {
                    // update lastScanMillis to now (simple strategy)
                    lastScanMillis = System.currentTimeMillis()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun update(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                repo.update(transaction)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}
