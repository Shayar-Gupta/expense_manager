package com.example.expensemanager.sms

data class SmsMessageData(
    val id: String,
    val address: String,
    val body: String,
    val dateMillis: Long
)
