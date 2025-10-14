package com.example.expensemanager.data

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

data class SmsRaw(val id: String?, val address: String?, val body: String?, val dateMillis: Long)

object SmsParser {
    // permissive amount regex; can be improved with bank-specific rules
    private val amountRegex = Regex("""([₹$€]|rs\.?|inr)?\s*([0-9]{1,3}(?:[, ]?[0-9]{3})*(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
    private val creditWords = listOf("credited","cr","deposit","received","refund","added")
    private val debitWords = listOf("debited","dr","purchase","paid","spent","withdrawn","txn","tx")

    private val merchantMap = mapOf(
        "amazon" to "Shopping",
        "flipkart" to "Shopping",
        "zomato" to "Food",
        "swiggy" to "Food",
        "uber" to "Transport",
        "ola" to "Transport",
        "hpcl" to "Fuel"
    )

    fun parse(sms: SmsRaw): TransactionEntity? {
        val body = sms.body?.trim() ?: return null
        val text = body.lowercase(Locale.getDefault())

        val amountMatch = amountRegex.find(text)
        val rawAmount = amountMatch?.groups?.get(2)?.value?.replace(",", "")?.replace(" ", "")
        val amount = rawAmount?.toDoubleOrNull() ?: return null // require amount to create transaction

        val type = when {
            creditWords.any { text.contains(it) } -> "Credit"
            debitWords.any { text.contains(it) } -> "Debit"
            else -> "Unknown"
        }

        // merchant heuristic
        val merchant = Regex("""(?:at|to|via)\s+([a-z0-9 &.\-]{2,60})""").find(text)?.groups?.get(1)?.value?.trim()
            ?: sms.address ?: "Unknown"

        val category = merchantMap.entries.find { (k, _) ->
            merchant.contains(k, ignoreCase = true) || text.contains(k, ignoreCase = true)
        }?.value ?: "Uncategorized"

        val confidence = listOf(
            if (amountMatch != null) 0.5 else 0.0,
            if (type != "Unknown") 0.3 else 0.0,
            if (!merchant.isNullOrBlank()) 0.2 else 0.0
        ).sum()

        val isoDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(sms.dateMillis))
        val messageId = generateId(sms)

        return TransactionEntity(
            messageId = messageId,
            amount = amount,
            category = category,
            type = type,
            date = isoDate,
            description = merchant,
            confidence = confidence,
            needsReview = confidence < 0.6
        )
    }

    private fun generateId(sms: SmsRaw): String {
        val base = listOfNotNull(sms.id, sms.address, sms.dateMillis.toString(), sms.body).joinToString("|")
        val md = MessageDigest.getInstance("SHA-256").digest(base.toByteArray())
        return md.joinToString("") { "%02x".format(it) }
    }
}
