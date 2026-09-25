package com.example.model

enum class TransactionType(val label: String) {
    EXPENSE("Expense"),
    INCOME("Income")
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    UPI_TRANSFER("UPI / Bank"),
    DIGITAL_WALLET("Digital Wallet")
}
