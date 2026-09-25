package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CarRental
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CatBills
import com.example.ui.theme.CatEducation
import com.example.ui.theme.CatEntertainment
import com.example.ui.theme.CatFood
import com.example.ui.theme.CatGroceries
import com.example.ui.theme.CatHealth
import com.example.ui.theme.CatHousing
import com.example.ui.theme.CatInvestment
import com.example.ui.theme.CatOther
import com.example.ui.theme.CatShopping
import com.example.ui.theme.CatTransport
import com.example.ui.theme.CatTravel

enum class ExpenseCategory(
    val displayName: String,
    val icon: ImageVector,
    val color: Color
) {
    FOOD_DINING("Food & Dining", Icons.Default.Restaurant, CatFood),
    GROCERIES("Groceries", Icons.Default.LocalGroceryStore, CatGroceries),
    SHOPPING("Shopping", Icons.Default.ShoppingBag, CatShopping),
    TRANSPORTATION("Transportation", Icons.Default.DirectionsCar, CatTransport),
    HOUSING_RENT("Housing & Rent", Icons.Default.Home, CatHousing),
    BILLS_UTILITIES("Bills & Utilities", Icons.Default.Receipt, CatBills),
    ENTERTAINMENT("Entertainment", Icons.Default.Movie, CatEntertainment),
    HEALTHCARE("Health & Wellness", Icons.Default.MedicalServices, CatHealth),
    TRAVEL("Travel & Holidays", Icons.Default.Flight, CatTravel),
    EDUCATION("Education", Icons.Default.School, CatEducation),
    PERSONAL_CARE("Personal Care", Icons.Default.Spa, CatShopping),
    INVESTMENT("Investments", Icons.Default.TrendingUp, CatInvestment),
    SALARY("Salary & Wages", Icons.Default.Work, CatInvestment),
    OTHER("Other", Icons.Default.MoreHoriz, CatOther);

    companion object {
        fun fromString(name: String): ExpenseCategory {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) }
                ?: OTHER
        }

        val expenseCategories = listOf(
            FOOD_DINING,
            GROCERIES,
            SHOPPING,
            TRANSPORTATION,
            HOUSING_RENT,
            BILLS_UTILITIES,
            ENTERTAINMENT,
            HEALTHCARE,
            TRAVEL,
            EDUCATION,
            PERSONAL_CARE,
            INVESTMENT,
            OTHER
        )

        val incomeCategories = listOf(
            SALARY,
            INVESTMENT,
            OTHER
        )
    }
}
