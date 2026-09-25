package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(entities = [Expense::class, Budget::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spendwise_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.expenseDao(), database.budgetDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(expenseDao: ExpenseDao, budgetDao: BudgetDao) {
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 60 * 60 * 1000L
            val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val currentMonthYear = monthYearFormat.format(Calendar.getInstance().time)

            // Seed initial budgets
            val initialBudgets = listOf(
                Budget(category = "ALL", monthlyLimit = 2400.0, monthYear = currentMonthYear),
                Budget(category = "FOOD_DINING", monthlyLimit = 400.0, monthYear = currentMonthYear),
                Budget(category = "GROCERIES", monthlyLimit = 450.0, monthYear = currentMonthYear),
                Budget(category = "HOUSING_RENT", monthlyLimit = 1250.0, monthYear = currentMonthYear),
                Budget(category = "TRANSPORTATION", monthlyLimit = 200.0, monthYear = currentMonthYear),
                Budget(category = "ENTERTAINMENT", monthlyLimit = 150.0, monthYear = currentMonthYear),
                Budget(category = "BILLS_UTILITIES", monthlyLimit = 200.0, monthYear = currentMonthYear),
                Budget(category = "SHOPPING", monthlyLimit = 250.0, monthYear = currentMonthYear)
            )
            budgetDao.insertAll(initialBudgets)

            // Seed initial realistic expenses & incomes for a lively experience
            val initialExpenses = listOf(
                Expense(
                    title = "Monthly Salary",
                    amount = 3850.0,
                    category = "SALARY",
                    type = "INCOME",
                    timestamp = now - (dayMillis * 18),
                    paymentMethod = "UPI_TRANSFER",
                    note = "Direct payroll deposit"
                ),
                Expense(
                    title = "Freelance Consulting",
                    amount = 650.0,
                    category = "INVESTMENT",
                    type = "INCOME",
                    timestamp = now - (dayMillis * 5),
                    paymentMethod = "UPI_TRANSFER",
                    note = "UI UX design client work"
                ),
                Expense(
                    title = "Apartment Rent",
                    amount = 1200.0,
                    category = "HOUSING_RENT",
                    type = "EXPENSE",
                    timestamp = now - (dayMillis * 20),
                    paymentMethod = "UPI_TRANSFER",
                    note = "September monthly rent"
                ),
                Expense(
                    title = "Supermarket Grocery",
                    amount = 138.45,
                    category = "GROCERIES",
                    type = "EXPENSE",
                    timestamp = now - (dayMillis * 1),
                    paymentMethod = "CREDIT_CARD",
                    note = "Weekly groceries, produce and dairy"
                ),
                Expense(
                    title = "Bistro Dinner with Friends",
                    amount = 64.50,
                    category = "FOOD_DINING",
                    type = "EXPENSE",
                    timestamp = now - (dayMillis * 2),
                    paymentMethod = "CREDIT_CARD",
                    note = "Italian pizza and wine"
                ),
                Expense(
                    title = "Metro Transit Pass",
                    amount = 75.0,
                    category = "TRANSPORTATION",
                    type = "EXPENSE",
                    timestamp = now - (dayMillis * 12),
                    paymentMethod = "DEBIT_CARD",
                    note = "Monthly subway & bus card"
                ),
                Expense(
                    title = "Electric & Gas Bill",
                    amount = 86.20,
                    category = "BILLS_UTILITIES",
                    type = "EXPENSE",
                    timestamp = now - (dayMillis * 8),
                    paymentMethod = "DEBIT_CARD",
                    note = "Monthly utility charges"
                ),
                Expense(
                    title = "Wireless Noise-Canceling Earbuds",
                    amount = 129.99,
                    category = "SHOPPING",
                    type = "EXPENSE",
                    timestamp = now - (dayMillis * 14),
                    paymentMethod = "CREDIT_CARD",
                    note = "Audio equipment upgrade"
                ),
                Expense(
                    title = "Streaming & Cloud Subscriptions",
                    amount = 28.98,
                    category = "ENTERTAINMENT",
                    type = "EXPENSE",
                    timestamp = now - (dayMillis * 9),
                    paymentMethod = "CREDIT_CARD",
                    note = "Music and video streaming"
                ),
                Expense(
                    title = "Specialty Coffee & Croissant",
                    amount = 9.25,
                    category = "FOOD_DINING",
                    type = "EXPENSE",
                    timestamp = now - (dayMillis * 0),
                    paymentMethod = "DIGITAL_WALLET",
                    note = "Morning espresso"
                ),
                Expense(
                    title = "Fuel & Car Wash",
                    amount = 48.0,
                    category = "TRANSPORTATION",
                    type = "EXPENSE",
                    timestamp = now - (dayMillis * 4),
                    paymentMethod = "CREDIT_CARD",
                    note = "Full tank refill"
                ),
                Expense(
                    title = "Pharmacy & Supplements",
                    amount = 32.50,
                    category = "HEALTHCARE",
                    type = "EXPENSE",
                    timestamp = now - (dayMillis * 6),
                    paymentMethod = "DEBIT_CARD",
                    note = "Multivitamins and medicine"
                ),
                Expense(
                    title = "Weekend Cinema Tickets",
                    amount = 27.0,
                    category = "ENTERTAINMENT",
                    type = "EXPENSE",
                    timestamp = now - (dayMillis * 3),
                    paymentMethod = "DIGITAL_WALLET",
                    note = "IMAX movie with popcorn"
                )
            )
            expenseDao.insertAll(initialExpenses)
        }
    }
}
