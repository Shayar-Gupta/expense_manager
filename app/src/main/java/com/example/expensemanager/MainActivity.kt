package com.example.expensemanager

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.example.expensemanager.ui.screens.MainScreen
import com.example.expensemanager.ui.theme.ComposeExpenseTheme
import com.example.expensemanager.viewmodel.TransactionsViewModel
import com.example.expensemanager.viewmodel.TransactionsViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var readSmsLauncher: ActivityResultLauncher<String>

    @SuppressLint("CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Step 1: Register permission launcher BEFORE Compose content
        readSmsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

        // ✅ Step 2: Load Compose content
        setContent {
            val vm: TransactionsViewModel =
                viewModel(factory = TransactionsViewModelFactory(application))


            ComposeExpenseTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen(
                        vm = vm,
                        onRequestSmsPermission = { requestSmsPermissionOrScan(vm) }
                    )
                }
            }

            // Optionally: automatically scan messages when the UI is ready
            LaunchedEffect(Unit) {
                vm.scanNow()
            }

            // ✅ Step 3: Auto-scan on app launch if permission already granted
            lifecycleScope.launch {
                if (isSmsPermissionGranted()) {
                    vm.scanNow()
                } else {
                    readSmsLauncher.launch(Manifest.permission.READ_SMS)
                }
            }
        }
    }

    // ✅ Helper: Checks if READ_SMS is already granted
    private fun isSmsPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    // ✅ Helper: Triggers a scan if permitted, else launches permission request
    private fun requestSmsPermissionOrScan(vm: TransactionsViewModel) {
        if (isSmsPermissionGranted()) {
            vm.scanNow()
        } else {
            readSmsLauncher.launch(Manifest.permission.READ_SMS)
        }
    }
}
