package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // "ALL" for overall budget or Category name (e.g. FOOD_DINING)
    val monthlyLimit: Double,
    val monthYear: String // "2026-09"
)
