package com.finanzas.app.domain.usecase

import com.finanzas.app.core.util.AppCategories
import com.finanzas.app.core.util.MoneyConverter
import com.finanzas.app.domain.model.AccountType
import com.finanzas.app.domain.model.DebtStatus
import com.finanzas.app.domain.model.FinancialSummary
import com.finanzas.app.domain.model.TransactionType
import com.finanzas.app.domain.repository.AccountRepository
import com.finanzas.app.domain.repository.CurrencyRepository
import com.finanzas.app.domain.repository.DebtRepository
import com.finanzas.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Calcula el resumen financiero integral, TODO expresado en USD.
 *
 * INVARIANTES:
 *
 *  INV-1 (conservacion de caja)
 *      El impacto de cada movimiento sobre el saldo debe coincidir con su
 *      impacto sobre ingresos/gastos:
 *        EXPENSE / CREDIT_PURCHASE -> gasto = monto + comision   (saldo: -(monto+comision))
 *        INCOME                    -> ingreso = monto - comision (saldo: +(monto-comision))
 *        TRANSFER / REMITTANCE     -> gasto = solo la comision   (el resto cambia de bolsillo)
 *
 *  INV-3 (homogeneidad dimensional)
 *      Ningun monto se suma sin pasar antes por MoneyConverter.toBase.
 */
class GetFinancialSummaryUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val debtRepository: DebtRepository,
    private val currencyRepository: CurrencyRepository
) {
    operator fun invoke(): Flow<FinancialSummary> {
        return combine(
            accountRepository.getAllAccountsFlow(),
            transactionRepository.getAllTransactionsWithDetailsFlow(),
            debtRepository.getAllDebtsWithInstallmentsFlow(),
            currencyRepository.getExchangeRateFlow()
        ) { accounts, allTransactions, debtsWithInstallments, rateInfo ->
            val trm = rateInfo.officialTrm

            var liquid = 0.0
            var creditDebt = 0.0

            for (account in accounts) {
                val enUsd = MoneyConverter.toBase(account.currentBalance, account.currency, trm)
                if (account.type == AccountType.CREDIT_CARD) {
                    creditDebt += enUsd
                } else {
                    liquid += enUsd
                }
            }

            var income = 0.0
            var expenses = 0.0

            for (item in allTransactions) {
                val tx = item.transaction
                val monedaOrigen = item.sourceAccount?.currency ?: item.targetAccount?.currency
                val monedaDestino = item.targetAccount?.currency ?: item.sourceAccount?.currency

                when (tx.type) {
                    TransactionType.INCOME -> {
                        // El saldo se acredita por (monto - comision): el ingreso neto es ese.
                        income += MoneyConverter.toBase(tx.amount - tx.feeAmount, monedaDestino, trm)
                    }

                    TransactionType.EXPENSE, TransactionType.CREDIT_PURCHASE -> {
                        // M-15: amortizar una deuda NO es un gasto de consumo, es un
                        // movimiento de balance (baja la caja y baja el pasivo en la
                        // misma cuantia, el patrimonio no cambia). Contarlo como gasto
                        // lo duplicaba con la compra original a credito.
                        if (tx.categoryId != AppCategories.DEBT_PAYMENT) {
                            expenses += MoneyConverter.toBase(tx.amount + tx.feeAmount, monedaOrigen, trm)
                        }
                    }

                    TransactionType.TRANSFER, TransactionType.REMITTANCE -> {
                        // El capital solo cambia de bolsillo; lo unico que se pierde
                        // de verdad es la comision (giro WU, transferencia interbancaria).
                        expenses += MoneyConverter.toBase(tx.feeAmount, monedaOrigen, trm)
                    }
                }
            }

            val standaloneDebts = debtsWithInstallments
                .filter { it.debt.status == DebtStatus.ACTIVE || it.debt.status == DebtStatus.OVERDUE }
                .sumOf { MoneyConverter.toBase(it.remainingTotalDebt, it.debt.currency, trm) }

            val totalDebts = creditDebt + standaloneDebts
            val netBalance = income - expenses
            val netWorth = liquid - totalDebts

            FinancialSummary(
                totalLiquidAssets = liquid,
                totalIncome = income,
                totalExpenses = expenses,
                totalCreditDebt = creditDebt,
                totalStandaloneDebts = standaloneDebts,
                totalDebts = totalDebts,
                netBalance = netBalance,
                netWorth = netWorth,
                totalAccountsCount = accounts.size,
                recentMovementsCount = allTransactions.size
            )
        }
    }
}
