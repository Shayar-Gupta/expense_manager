package com.example.expensemanager.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.telephony.SmsMessage
import android.util.Log

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle: Bundle? = intent.extras
            try {
                val pdus = bundle?.get("pdus") as? Array<*>
                pdus?.forEach { pdu ->
                    val sms = SmsMessage.createFromPdu(pdu as ByteArray)
                    val msgBody = sms.messageBody
                    val sender = sms.originatingAddress
                    val timestamp = sms.timestampMillis
                    Log.d("SmsReceiver", "SMS from $sender: $msgBody")
                }
            } catch (e: Exception) {
                Log.e("SmsReceiver", "Error parsing incoming SMS", e)
            }
        }
    }
}
