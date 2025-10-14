package com.example.expensemanager.repo

import android.content.Context
import com.example.expensemanager.data.TransactionDao
import com.example.expensemanager.data.TransactionEntity
import com.example.expensemanager.parser.Parser
import com.example.expensemanager.sms.SmsReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TransactionRepository(private val dao: TransactionDao) {

    fun streamAll(): Flow<List<TransactionEntity>> = dao.streamAll()

    /**
     * Scans SMS inbox for new messages, parses them, deduplicates by messageId,
     * and inserts only new transactions. Returns number of inserted items.
     */
    suspend fun scanAndSave(context: Context, sinceMillis: Long? = null): Int {
        return withContext(Dispatchers.IO) {
            val msgs = SmsReader.readAllMessages(context, sinceMillis)
            var inserted = 0
            for (sms in msgs) {
                try {
                    val parsed = SmsParser.parse(sms) ?: continue
                    val exists = dao.exists(parsed.messageId)
                    if (!exists) {
                        val id = dao.insert(parsed)
                        if (id != -1L) inserted++
                    }
                } catch (ex: Exception) {
                    // Keep scanning on error; log in real app
                    ex.printStackTrace()
                }
            }
            inserted
        }
    }

    suspend fun update(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        dao.update(transaction)
    }
}
