package com.example.expensemanager.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.expensemanager.data.TransactionEntity
import com.example.expensemanager.viewmodel.TransactionsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(vm: TransactionsViewModel, onRequestSmsPermission: () -> Unit) {
    val grouped by vm.groupedByDate.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Expense Manager") }, actions = {
                IconButton(onClick = { onRequestSmsPermission() }) { Icon(Icons.Default.Refresh, contentDescription = "Request SMS") }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { vm.scanNow() }) { Text("Scan") }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            grouped.forEach { (date, list) ->
                item { DateHeader(date) }
                items(list) { tx -> TransactionRow(tx) }
            }
        }
    }
}

@Composable
fun DateHeader(date: String) {
    Text(date, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(12.dp))
}

@Composable
fun TransactionRow(tx: TransactionEntity) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 6.dp)
        .clickable { /* open details */ }) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text(tx.description); Text(tx.category, style = MaterialTheme.typography.bodySmall) }
            Column { Text("${if (tx.type == "Credit") "+" else "-"}₹${"%.2f".format(tx.amount)}") }
        }
    }
}
