package com.finanzas.app.data.mapper

import com.finanzas.app.data.local.entity.AccountEntity
import com.finanzas.app.data.local.entity.CategoryEntity
import com.finanzas.app.data.local.entity.TransactionEntity
import com.finanzas.app.domain.model.Account
import com.finanzas.app.domain.model.Category
import com.finanzas.app.domain.model.Transaction

fun AccountEntity.toDomain(): Account = Account(
    id = id,
    name = name,
    type = type,
    currency = currency,
    currentBalance = currentBalance,
    colorHex = colorHex,
    isDefault = isDefault
)

fun Account.toEntity(): AccountEntity = AccountEntity(
    id = id,
    name = name,
    type = type,
    currency = currency,
    currentBalance = currentBalance,
    colorHex = colorHex,
    isDefault = isDefault,
    updatedAt = System.currentTimeMillis()
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    iconName = iconName,
    type = type,
    colorHex = colorHex
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    iconName = iconName,
    type = type,
    colorHex = colorHex
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    type = type,
    sourceAccountId = sourceAccountId,
    targetAccountId = targetAccountId,
    amount = amount,
    categoryId = categoryId,
    feeAmount = feeAmount,
    targetAmount = targetAmount,
    targetCurrency = targetCurrency,
    exchangeRate = exchangeRate,
    installments = installments,
    timestamp = timestamp,
    note = note,
    isSynced = isSynced,
    syncTimestamp = syncTimestamp
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    type = type,
    sourceAccountId = sourceAccountId,
    targetAccountId = targetAccountId,
    amount = amount,
    categoryId = categoryId,
    feeAmount = feeAmount,
    targetAmount = targetAmount,
    targetCurrency = targetCurrency,
    exchangeRate = exchangeRate,
    installments = installments,
    timestamp = timestamp,
    note = note,
    isSynced = isSynced,
    syncTimestamp = syncTimestamp
)

fun com.finanzas.app.data.local.entity.DebtEntity.toDomain(): com.finanzas.app.domain.model.Debt = com.finanzas.app.domain.model.Debt(
    id = id,
    title = title,
    entityName = entityName,
    originalPrincipal = originalPrincipal,
    interestRatePercentage = interestRatePercentage,
    totalInstallments = totalInstallments,
    installmentAmount = installmentAmount,
    totalWithInterest = totalWithInterest,
    startDate = startDate,
    dueDayOfMonth = dueDayOfMonth,
    currency = currency,
    accountId = accountId,
    notes = notes,
    status = status
)

fun com.finanzas.app.domain.model.Debt.toEntity(): com.finanzas.app.data.local.entity.DebtEntity = com.finanzas.app.data.local.entity.DebtEntity(
    id = id,
    title = title,
    entityName = entityName,
    originalPrincipal = originalPrincipal,
    interestRatePercentage = interestRatePercentage,
    totalInstallments = totalInstallments,
    installmentAmount = installmentAmount,
    totalWithInterest = totalWithInterest,
    startDate = startDate,
    dueDayOfMonth = dueDayOfMonth,
    currency = currency,
    accountId = accountId,
    notes = notes,
    status = status
)

fun com.finanzas.app.data.local.entity.DebtInstallmentEntity.toDomain(): com.finanzas.app.domain.model.DebtInstallment = com.finanzas.app.domain.model.DebtInstallment(
    id = id,
    debtId = debtId,
    installmentNumber = installmentNumber,
    dueDate = dueDate,
    principalAmount = principalAmount,
    interestAmount = interestAmount,
    lateFeeAmount = lateFeeAmount,
    isPaid = isPaid,
    paidDate = paidDate,
    actualPaidAmount = actualPaidAmount,
    paymentAccountId = paymentAccountId,
    notes = notes
)

fun com.finanzas.app.domain.model.DebtInstallment.toEntity(): com.finanzas.app.data.local.entity.DebtInstallmentEntity = com.finanzas.app.data.local.entity.DebtInstallmentEntity(
    id = id,
    debtId = debtId,
    installmentNumber = installmentNumber,
    dueDate = dueDate,
    principalAmount = principalAmount,
    interestAmount = interestAmount,
    lateFeeAmount = lateFeeAmount,
    isPaid = isPaid,
    paidDate = paidDate,
    actualPaidAmount = actualPaidAmount,
    paymentAccountId = paymentAccountId,
    notes = notes
)

