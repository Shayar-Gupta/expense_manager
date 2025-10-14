package com.example.expensemanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Core DB entity. messageId is the unique id derived from sms provider or generated hash.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val messageId: String,
    val amount: Double,
    val category: String,
    val type: String,        // "Credit" | "Debit" | "Unknown"
    val date: String,        // ISO date "YYYY-MM-DD" for grouping
    val description: String, // merchant or brief text
    val confidence: Double = 1.0,
    val needsReview: Boolean = false
)
