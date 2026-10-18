package com.finanzas.app.data.local.converters

import androidx.room.TypeConverter
import com.finanzas.app.domain.model.AccountType
import com.finanzas.app.domain.model.TransactionType

class Converters {
    @TypeConverter
    fun fromAccountType(type: AccountType?): String? = type?.name

    @TypeConverter
    fun toAccountType(name: String?): AccountType? = name?.let {
        try {
            AccountType.valueOf(it)
        } catch (e: IllegalArgumentException) {
            AccountType.CASH
        }
    }

    @TypeConverter
    fun fromTransactionType(type: TransactionType?): String? = type?.name

    @TypeConverter
    fun toTransactionType(name: String?): TransactionType? = name?.let {
        try {
            TransactionType.valueOf(it)
        } catch (e: IllegalArgumentException) {
            TransactionType.EXPENSE
        }
    }

    @TypeConverter
    fun fromDebtStatus(status: com.finanzas.app.domain.model.DebtStatus?): String? = status?.name

    @TypeConverter
    fun toDebtStatus(name: String?): com.finanzas.app.domain.model.DebtStatus? = name?.let {
        try {
            com.finanzas.app.domain.model.DebtStatus.valueOf(it)
        } catch (e: IllegalArgumentException) {
            com.finanzas.app.domain.model.DebtStatus.ACTIVE
        }
    }
}
