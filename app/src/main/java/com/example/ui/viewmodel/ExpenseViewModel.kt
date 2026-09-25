package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Budget
import com.example.data.Expense
import com.example.data.ExpenseRepository
import com.example.model.ExpenseCategory
import com.example.model.PaymentMethod
import com.example.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class DateFilterPeriod(val displayName: String) {
    THIS_MONTH("This Month"),
    THIS_WEEK("This Week"),
    TODAY("Today"),
    LAST_30_DAYS("Last 30 Days"),
    ALL("All Time")
}

data class DailySpend(
    val dayLabel: String,
    val dateMillis: Long,
    val amount: Double
)

data class CategorySpend(
    val category: ExpenseCategory,
    val amount: Double,
    val percentage: Float,
    val transactionCount: Int
)

data class ExpenseUiState(
    val allExpenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val overallBudget: Budget? = null,
    val currentPeriod: DateFilterPeriod = DateFilterPeriod.THIS_MONTH,
    val searchQuery: String = "",
    val categoryFilter: String? = null,
    val typeFilter: TransactionType? = null,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0,
    val monthlySpent: Double = 0.0,
    val monthlyBudgetLimit: Double = 2400.0,
    val budgetProgress: Float = 0f,
    val remainingMonthlyBudget: Double = 0.0,
    val daysRemainingInMonth: Int = 1,
    val dailySafeSpend: Double = 0.0,
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val dailySpendingChart: List<DailySpend> = emptyList(),
    val topSpendingCategory: CategorySpend? = null,
    val isAddSheetOpen: Boolean = false,
    val isBudgetDialogOpen: Boolean = false,
    val editingExpense: Expense? = null,
    val editingBudget: Budget? = null,
    val defaultAddType: TransactionType = TransactionType.EXPENSE,
    val currentTab: Int = 0
)

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ExpenseRepository
    private val currentMonthYear: String

    init {
        val database = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(database.expenseDao(), database.budgetDao())
        val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        currentMonthYear = monthYearFormat.format(Calendar.getInstance().time)
    }

    private val _currentPeriod = MutableStateFlow(DateFilterPeriod.THIS_MONTH)
    private val _searchQuery = MutableStateFlow("")
    private val _categoryFilter = MutableStateFlow<String?>(null)
    private val _typeFilter = MutableStateFlow<TransactionType?>(null)
    private val _currentTab = MutableStateFlow(0)

    private val _isAddSheetOpen = MutableStateFlow(false)
    private val _isBudgetDialogOpen = MutableStateFlow(false)
    private val _editingExpense = MutableStateFlow<Expense?>(null)
    private val _editingBudget = MutableStateFlow<Budget?>(null)
    private val _defaultAddType = MutableStateFlow(TransactionType.EXPENSE)

    val uiState: StateFlow<ExpenseUiState> = combine(
        repository.allExpenses,
        repository.allBudgets,
        _currentPeriod,
        _searchQuery,
        _categoryFilter,
        _typeFilter,
        _currentTab,
        _isAddSheetOpen,
        _isBudgetDialogOpen,
        _editingExpense,
        _editingBudget,
        _defaultAddType
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        val allExpenses = args[0] as List<Expense>
        @Suppress("UNCHECKED_CAST")
        val budgets = args[1] as List<Budget>
        val period = args[2] as DateFilterPeriod
        val query = args[3] as String
        val catFilter = args[4] as? String
        val typeFilt = args[5] as? TransactionType
        val tab = args[6] as Int
        val isAddOpen = args[7] as Boolean
        val isBudgetOpen = args[8] as Boolean
        val editExp = args[9] as? Expense
        val editBudg = args[10] as? Budget
        val defType = args[11] as TransactionType

        calculateUiState(
            allExpenses = allExpenses,
            budgets = budgets,
            period = period,
            query = query,
            catFilter = catFilter,
            typeFilt = typeFilt,
            tab = tab,
            isAddOpen = isAddOpen,
            isBudgetOpen = isBudgetOpen,
            editExp = editExp,
            editBudg = editBudg,
            defType = defType
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseUiState()
    )

    private fun calculateUiState(
        allExpenses: List<Expense>,
        budgets: List<Budget>,
        period: DateFilterPeriod,
        query: String,
        catFilter: String?,
        typeFilt: TransactionType?,
        tab: Int,
        isAddOpen: Boolean,
        isBudgetOpen: Boolean,
        editExp: Expense?,
        editBudg: Budget?,
        defType: TransactionType
    ): ExpenseUiState {
        val now = Calendar.getInstance()
        val (periodStart, periodEnd) = getPeriodRange(period)

        // Filter for the selected period
        val periodExpenses = allExpenses.filter { it.timestamp in periodStart..periodEnd }

        // Total Income and Expense for the selected period
        val totalIncome = periodExpenses.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = periodExpenses.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense

        // Calculate this current month's total expenses for budget checking
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val monthEnd = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        val monthlyExpensesList = allExpenses.filter {
            it.timestamp in monthStart..monthEnd && it.type == "EXPENSE"
        }
        val monthlySpent = monthlyExpensesList.sumOf { it.amount }

        // Overall budget
        val overallBudget = budgets.find { it.category == "ALL" }
        val budgetLimit = overallBudget?.monthlyLimit ?: 2400.0
        val budgetProgress = if (budgetLimit > 0) (monthlySpent / budgetLimit).toFloat() else 0f
        val remainingBudget = maxOf(0.0, budgetLimit - monthlySpent)

        // Days remaining in month
        val todayDay = now.get(Calendar.DAY_OF_MONTH)
        val maxDays = now.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysRemaining = maxOf(1, maxDays - todayDay + 1)
        val dailySafe = remainingBudget / daysRemaining

        // Filtered transactions for Transactions screen (respects query, category, type filter)
        val filtered = periodExpenses.filter { exp ->
            val matchesQuery = query.isBlank() ||
                    exp.title.contains(query, ignoreCase = true) ||
                    exp.note.contains(query, ignoreCase = true)
            val matchesCat = catFilter == null || exp.category.equals(catFilter, ignoreCase = true)
            val matchesType = typeFilt == null || exp.type.equals(typeFilt.name, ignoreCase = true)
            matchesQuery && matchesCat && matchesType
        }

        // Category breakdown for expenses
        val expenseTransactions = periodExpenses.filter { it.type == "EXPENSE" }
        val groupedByCat = expenseTransactions.groupBy { ExpenseCategory.fromString(it.category) }
        val categoryBreakdown = groupedByCat.map { (cat, list) ->
            val sum = list.sumOf { it.amount }
            val pct = if (totalExpense > 0) (sum / totalExpense).toFloat() else 0f
            CategorySpend(
                category = cat,
                amount = sum,
                percentage = pct,
                transactionCount = list.size
            )
        }.sortedByDescending { it.amount }

        val topCategory = categoryBreakdown.firstOrNull()

        // Daily spending chart (last 7 days or days in week)
        val dailySpendList = calculateDailySpendChart(periodExpenses)

        return ExpenseUiState(
            allExpenses = allExpenses,
            filteredExpenses = filtered,
            budgets = budgets,
            overallBudget = overallBudget,
            currentPeriod = period,
            searchQuery = query,
            categoryFilter = catFilter,
            typeFilter = typeFilt,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            netBalance = netBalance,
            monthlySpent = monthlySpent,
            monthlyBudgetLimit = budgetLimit,
            budgetProgress = budgetProgress,
            remainingMonthlyBudget = remainingBudget,
            daysRemainingInMonth = daysRemaining,
            dailySafeSpend = dailySafe,
            categoryBreakdown = categoryBreakdown,
            dailySpendingChart = dailySpendList,
            topSpendingCategory = topCategory,
            isAddSheetOpen = isAddOpen,
            isBudgetDialogOpen = isBudgetOpen,
            editingExpense = editExp,
            editingBudget = editBudg,
            defaultAddType = defType,
            currentTab = tab
        )
    }

    private fun calculateDailySpendChart(expenses: List<Expense>): List<DailySpend> {
        val result = mutableListOf<DailySpend>()
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        // Past 7 days
        for (i in 6 downTo 0) {
            val targetCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val startOfDay = (targetCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = (targetCal.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val dayTotal = expenses.filter {
                it.type == "EXPENSE" && it.timestamp in startOfDay..endOfDay
            }.sumOf { it.amount }

            val label = if (i == 0) "Today" else dayFormat.format(targetCal.time)
            result.add(DailySpend(dayLabel = label, dateMillis = startOfDay, amount = dayTotal))
        }
        return result
    }

    private fun getPeriodRange(period: DateFilterPeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val end = cal.timeInMillis

        val start = when (period) {
            DateFilterPeriod.TODAY -> {
                cal.apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            DateFilterPeriod.THIS_WEEK -> {
                cal.apply {
                    set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            DateFilterPeriod.THIS_MONTH -> {
                cal.apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
            }
            DateFilterPeriod.LAST_30_DAYS -> {
                cal.apply {
                    add(Calendar.DAY_OF_YEAR, -30)
                }.timeInMillis
            }
            DateFilterPeriod.ALL -> 0L
        }
        return Pair(start, end)
    }

    // Actions
    fun setPeriod(period: DateFilterPeriod) {
        _currentPeriod.value = period
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = if (_categoryFilter.value == category) null else category
    }

    fun setTypeFilter(type: TransactionType?) {
        _typeFilter.value = if (_typeFilter.value == type) null else type
    }

    fun setTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun openAddSheet(type: TransactionType = TransactionType.EXPENSE) {
        _defaultAddType.value = type
        _editingExpense.value = null
        _isAddSheetOpen.value = true
    }

    fun openEditSheet(expense: Expense) {
        _editingExpense.value = expense
        _defaultAddType.value = if (expense.type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
        _isAddSheetOpen.value = true
    }

    fun closeAddSheet() {
        _isAddSheetOpen.value = false
        _editingExpense.value = null
    }

    fun openBudgetDialog(budget: Budget? = null) {
        _editingBudget.value = budget
        _isBudgetDialogOpen.value = true
    }

    fun closeBudgetDialog() {
        _isBudgetDialogOpen.value = false
        _editingBudget.value = null
    }

    fun saveExpense(
        id: Long = 0,
        title: String,
        amount: Double,
        category: String,
        type: String,
        timestamp: Long,
        paymentMethod: String,
        note: String
    ) {
        viewModelScope.launch {
            val expense = Expense(
                id = id,
                title = title.trim(),
                amount = amount,
                category = category,
                type = type,
                timestamp = timestamp,
                paymentMethod = paymentMethod,
                note = note.trim()
            )
            if (id == 0L) {
                repository.insertExpense(expense)
            } else {
                repository.updateExpense(expense)
            }
            closeAddSheet()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun saveBudget(id: Long = 0, category: String, monthlyLimit: Double) {
        viewModelScope.launch {
            val budget = Budget(
                id = id,
                category = category,
                monthlyLimit = monthlyLimit,
                monthYear = currentMonthYear
            )
            repository.upsertBudget(budget)
            closeBudgetDialog()
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudgetById(id)
        }
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.resetToSampleData()
        }
    }

    companion object {
        fun formatCurrency(amount: Double): String {
            val format = NumberFormat.getCurrencyInstance(Locale.US)
            return format.format(amount)
        }

        fun formatDate(timestamp: Long): String {
            val cal = Calendar.getInstance()
            val today = cal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val yesterday = today - (24 * 60 * 60 * 1000L)

            return when {
                timestamp >= today -> {
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    "Today, ${timeFormat.format(timestamp)}"
                }
                timestamp >= yesterday -> {
                    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                    "Yesterday, ${timeFormat.format(timestamp)}"
                }
                else -> {
                    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                    dateFormat.format(timestamp)
                }
            }
        }
    }
}
