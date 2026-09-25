package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.TransactionType
import com.example.ui.components.AddEditExpenseSheet
import com.example.ui.components.SetBudgetDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ExpenseViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                ExpenseApp()
            }
        }
    }
}

@Composable
fun ExpenseApp(viewModel: ExpenseViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle back button: if not on Dashboard, switch to Dashboard
    BackHandler(enabled = uiState.currentTab != 0) {
        viewModel.setTab(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            if (uiState.currentTab != 1) { // Show on Dashboard, Transactions, and Budgets
                FloatingActionButton(
                    onClick = { viewModel.openAddSheet(TransactionType.EXPENSE) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("global_add_expense_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Transaction",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                windowInsets = WindowInsets.navigationBars,
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = uiState.currentTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard") },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Analytics") },
                    label = { Text("Analytics") },
                    modifier = Modifier.testTag("nav_tab_analytics")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Transactions") },
                    label = { Text("Transactions") },
                    modifier = Modifier.testTag("nav_tab_transactions")
                )
                NavigationBarItem(
                    selected = uiState.currentTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Budgets") },
                    label = { Text("Budgets") },
                    modifier = Modifier.testTag("nav_tab_budgets")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                0 -> DashboardScreen(
                    state = uiState,
                    onAddExpense = { type -> viewModel.openAddSheet(type) },
                    onEditExpense = { expense -> viewModel.openEditSheet(expense) },
                    onDeleteExpense = { expense -> viewModel.deleteExpense(expense) },
                    onOpenBudget = { viewModel.openBudgetDialog(uiState.overallBudget) },
                    onNavigateToTransactions = { viewModel.setTab(2) },
                    onResetSampleData = { viewModel.resetSampleData() }
                )
                1 -> AnalyticsScreen(
                    state = uiState,
                    onPeriodSelected = { period -> viewModel.setPeriod(period) }
                )
                2 -> TransactionsScreen(
                    state = uiState,
                    onSearchChange = { query -> viewModel.setSearchQuery(query) },
                    onCategoryFilterChange = { category -> viewModel.setCategoryFilter(category) },
                    onTypeFilterChange = { type -> viewModel.setTypeFilter(type) },
                    onEditExpense = { expense -> viewModel.openEditSheet(expense) },
                    onDeleteExpense = { expense -> viewModel.deleteExpense(expense) }
                )
                3 -> BudgetsScreen(
                    state = uiState,
                    onOpenBudgetDialog = { budget -> viewModel.openBudgetDialog(budget) }
                )
            }

            // Add or Edit Expense Bottom Sheet
            if (uiState.isAddSheetOpen) {
                AddEditExpenseSheet(
                    initialExpense = uiState.editingExpense,
                    defaultType = uiState.defaultAddType,
                    onDismiss = { viewModel.closeAddSheet() },
                    onSave = { id, title, amount, category, type, timestamp, paymentMethod, note ->
                        viewModel.saveExpense(
                            id = id,
                            title = title,
                            amount = amount,
                            category = category,
                            type = type,
                            timestamp = timestamp,
                            paymentMethod = paymentMethod,
                            note = note
                        )
                    }
                )
            }

            // Set Budget Dialog
            if (uiState.isBudgetDialogOpen) {
                SetBudgetDialog(
                    initialBudget = uiState.editingBudget,
                    onDismiss = { viewModel.closeBudgetDialog() },
                    onSave = { id, category, limit ->
                        viewModel.saveBudget(id, category, limit)
                    },
                    onDelete = { budgetId ->
                        viewModel.deleteBudget(budgetId)
                    }
                )
            }
        }
    }
}
