package com.example.expensemanager

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensemanager.ui.screens.MainScreen
import com.example.expensemanager.viewmodel.TransactionsViewModel

class MainActivity : ComponentActivity() {
    private val readSmsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // nothing here; ViewModel.scanNow() called from UI on user action
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val vm: TransactionsViewModel = viewModel(factory = TransactionsViewModelFactory(application))
            MyAppTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen(vm = vm, onRequestSmsPermission = { readSmsLauncher.launch(Manifest.permission.READ_SMS) })
                }
            }
        }
    }
}
