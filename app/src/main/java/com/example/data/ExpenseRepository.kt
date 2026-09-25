package com.example.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao
) {
    val allExpenses: Flow<List<Expense>> = expenseDao.getAllExpenses()
    val allBudgets: Flow<List<Budget>> = budgetDao.getAllBudgets()

    fun getBudgetsForMonth(monthYear: String): Flow<List<Budget>> =
        budgetDao.getBudgetsForMonth(monthYear)

    suspend fun insertExpense(expense: Expense): Long =
        expenseDao.insertExpense(expense)

    suspend fun updateExpense(expense: Expense) =
        expenseDao.updateExpense(expense)

    suspend fun deleteExpense(expense: Expense) =
        expenseDao.deleteExpense(expense)

    suspend fun deleteExpenseById(id: Long) =
        expenseDao.deleteExpenseById(id)

    suspend fun getExpenseById(id: Long): Expense? =
        expenseDao.getExpenseById(id)

    suspend fun upsertBudget(budget: Budget): Long =
        budgetDao.insertBudget(budget)

    suspend fun deleteBudgetById(id: Long) =
        budgetDao.deleteBudgetById(id)

    suspend fun clearAll() {
        expenseDao.deleteAllExpenses()
    }

    suspend fun resetToSampleData() {
        expenseDao.deleteAllExpenses()
        AppDatabase.populateInitialData(expenseDao, budgetDao)
    }
}
