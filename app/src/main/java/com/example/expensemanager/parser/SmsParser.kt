package com.example.expensemanager.parser

import com.example.expensemanager.data.TransactionEntity
import com.example.expensemanager.sms.SmsMessageData
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * Parses SMS messages into TransactionEntity objects.
 * Detects credit/debit transactions, merchant, and category with reasonable accuracy.
 */

object SmsParser {

    // Regex for detecting amounts in formats like ₹1,000.50 or Rs. 500
    private val amountRegex = Regex(
        """([₹$€]|rs\.?|inr)?\s*([0-9]{1,3}(?:[, ]?[0-9]{3})*(?:\.[0-9]{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    // Keyword-based heuristics
    private val creditKeywords = listOf("credited", "cr", "deposit", "received", "refund", "added")
    private val debitKeywords = listOf("debited", "dr", "purchase", "spent", "withdrawn", "paid", "txn", "tx")

    // Basic merchant-to-category mapping
    private val merchantCategoryMap = mapOf(
        "amazon" to "Shopping",
        "flipkart" to "Shopping",
        "zomato" to "Food",
        "swiggy" to "Food",
        "ola" to "Transport",
        "uber" to "Transport",
        "hpcl" to "Fuel",
        "bharat" to "Fuel",
        "shell" to "Fuel"
    )

    /**
     * Parses an SMS message and returns a TransactionEntity, or null if parsing fails.
     */
    fun parseSmsToTransaction(sms: SmsMessageData): TransactionEntity? {
        val body = sms.body?.trim() ?: return null
        val text = body.lowercase(Locale.getDefault())

        // Extract amount
        val amountMatch = amountRegex.find(text)
        val rawAmount = amountMatch?.groups?.get(2)?.value
        val amount = rawAmount?.replace(",", "")?.replace(" ", "")?.toDoubleOrNull()
            ?: return null  // Skip messages without a valid amount

        // Determine type (credit/debit)
        val type = when {
            creditKeywords.any { text.contains(it) } -> "Credit"
            debitKeywords.any { text.contains(it) } -> "Debit"
            else -> "Unknown"
        }

        // Extract merchant name
        val merchant = Regex("""(?:at|to|via)\s+([a-z0-9 &.\-]{2,60})""")
            .find(text)?.groups?.get(1)?.value?.trim()
            ?: sms.address ?: "Unknown"

        // Categorize transaction
        val category = merchantCategoryMap.entries.find {
            merchant.contains(it.key, ignoreCase = true) || text.contains(it.key, ignoreCase = true)
        }?.value ?: "Uncategorized"

        // Format date (yyyy-MM-dd)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(sms.dateMillis))

        // Generate unique message ID (used to avoid duplicates)
        val messageId = generateSourceId(sms)

        return TransactionEntity(
            messageId = messageId,
            amount = amount,
            category = category,
            type = type,
            date = date,
            description = merchant
        )
    }

    /**
     * Creates a unique hash from SMS contents to prevent duplicate insertions.
     */
    private fun generateSourceId(sms: SmsMessageData): String {
        val base = "${sms.address}|${sms.dateMillis}|${sms.body.orEmpty()}"
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(base.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
