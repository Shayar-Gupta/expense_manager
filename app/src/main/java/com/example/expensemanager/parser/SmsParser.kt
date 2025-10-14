package com.example.expensemanager.parser

import com.example.expensemanager.data.TransactionEntity
import com.example.expensemanager.sms.SmsMessageData
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

data class SmsRaw(
    val id: String?,
    val address: String?,
    val body: String?,
    val dateMillis: Long
)

object SmsParser {

    private val amountRegex = Regex(
        "([₹$€]|rs\\.?|inr)?\\s*([0-9]{1,3}(?:[,\\s][0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)",
        RegexOption.IGNORE_CASE
    )

    private val creditKeywords = listOf("credited", "cr", "deposit", "received", "refund")
    private val debitKeywords = listOf("debited", "dr", "purchase", "spent", "withdrawn", "paid", "txn", "tx")

    private val merchantCategoryMap = mapOf(
        "amazon" to "Shopping",
        "flipkart" to "Shopping",
        "zomato" to "Food",
        "swiggy" to "Food",
        "ola" to "Transport",
        "uber" to "Transport",
        "hpcl" to "Fuel"
    )

    fun parseSmsToTransaction(sms: SmsMessageData): TransactionEntity? {
        val body = sms.body ?: return null
        val text = body.lowercase(Locale.getDefault())

        // Extract amount
        val amountMatch = amountRegex.find(text)
        val rawAmount = amountMatch?.groups?.get(2)?.value
        val amount = rawAmount?.replace(",", "")?.toDoubleOrNull() ?: return null

        // Detect credit/debit
        val type = when {
            creditKeywords.any { text.contains(it) } -> "Credit"
            debitKeywords.any { text.contains(it) } -> "Debit"
            else -> "Unknown"
        }

        // Extract merchant/payee
        val merchant = Regex("(?:at|to|via)\\s+([a-z0-9 &.-]{2,40})")
            .find(text)?.groups?.get(1)?.value?.trim()
            ?: sms.address ?: "Unknown"

        // Categorize
        val category = merchantCategoryMap.entries.find {
            merchant.contains(it.key, true) || text.contains(it.key, true)
        }?.value ?: "Uncategorized"

        // Format date
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(sms.dateMillis))

        // Generate unique hash for duplication checks
        val messageId = generateSourceId(sms)

        // Create entity
        return TransactionEntity(
            messageId = messageId,
            amount = amount,
            category = category,
            type = type,
            date = date,
            description = merchant
        )
    }

    private fun generateSourceId(sms: SmsMessageData): String {
        val base = (sms.address ?: "") + "|" + sms.dateMillis + "|" + (sms.body ?: "")
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(base.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
