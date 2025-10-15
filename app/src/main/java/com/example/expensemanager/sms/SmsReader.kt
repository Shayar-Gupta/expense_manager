package com.example.expensemanager.sms

import android.content.Context
import android.provider.Telephony
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SmsReader {
    /**
     * Read SMS from inbox using Telephony provider.
     * Returns newest-first ordering.
     */
    suspend fun readAllMessages(context: Context, sinceMillis: Long? = null): List<SmsMessageData> =
        withContext(Dispatchers.IO) {
            val list = mutableListOf<SmsMessageData>()
            val uri = Telephony.Sms.Inbox.CONTENT_URI
            val projection = arrayOf(Telephony.Sms._ID, Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE)
            val selection = if (sinceMillis != null) "date > ?" else null
            val selectionArgs = if (sinceMillis != null) arrayOf(sinceMillis.toString()) else null
            val sortOrder = "date DESC"

            context.contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(Telephony.Sms._ID)
                val addrIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
                val bodyIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
                val dateIdx = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
                while (cursor.moveToNext()) {
                    val id = cursor.getString(idIdx)
                    val address = cursor.getString(addrIdx)
                    val body = cursor.getString(bodyIdx)
                    val date = cursor.getLong(dateIdx)
                    list.add(SmsMessageData(id, address, body, date))
                }
            }
            list
        }
}
