package com.finanzas.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.finanzas.app.core.util.AppCategories
import com.finanzas.app.core.util.CurrencyFormatter
import com.finanzas.app.data.local.entity.DebtEntity
import com.finanzas.app.data.local.entity.DebtInstallmentEntity
import com.finanzas.app.data.local.entity.TransactionEntity
import com.finanzas.app.domain.model.DebtStatus
import com.finanzas.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
abstract class DebtDao {

    @Query("SELECT * FROM debts ORDER BY startDate DESC")
    abstract fun getAllDebtsFlow(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :debtId LIMIT 1")
    abstract suspend fun getDebtById(debtId: String): DebtEntity?

    @Query("SELECT * FROM debts WHERE id = :debtId LIMIT 1")
    abstract fun getDebtFlow(debtId: String): Flow<DebtEntity?>

    @Query("SELECT * FROM debt_installments ORDER BY dueDate ASC")
    abstract fun getAllInstallmentsFlow(): Flow<List<DebtInstallmentEntity>>

    @Query("SELECT * FROM debt_installments WHERE debtId = :debtId ORDER BY installmentNumber ASC")
    abstract fun getInstallmentsForDebtFlow(debtId: String): Flow<List<DebtInstallmentEntity>>

    @Query("SELECT * FROM debt_installments WHERE debtId = :debtId ORDER BY installmentNumber ASC")
    abstract suspend fun getInstallmentsForDebt(debtId: String): List<DebtInstallmentEntity>

    @Query("SELECT * FROM debt_installments WHERE id = :installmentId LIMIT 1")
    abstract suspend fun getInstallmentById(installmentId: String): DebtInstallmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDebt(debt: DebtEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertInstallments(installments: List<DebtInstallmentEntity>)

    @Update
    abstract suspend fun updateDebt(debt: DebtEntity)

    @Update
    abstract suspend fun updateInstallment(installment: DebtInstallmentEntity)

    @Query("DELETE FROM debts WHERE id = :debtId")
    abstract suspend fun deleteDebtById(debtId: String)

    @Transaction
    open suspend fun insertDebtWithInstallmentsAtomic(
        debt: DebtEntity,
        installments: List<DebtInstallmentEntity>
    ) {
        insertDebt(debt)
        insertInstallments(installments)
    }

    // ------------------------------------------------------------------
    // Motor de amortizacion
    // ------------------------------------------------------------------

    /**
     * Aplica [amount] (ya libre de mora) sobre las cuotas pendientes en orden de
     * vencimiento: cubre cuotas completas mientras alcance y deja la ultima
     * parcialmente amortizada. Devuelve el sobrante no aplicado.
     *
     * Es el metodo que hace que un abono sobre una deuda de CUOTAS FIJAS reduzca
     * realmente la deuda. Antes el dinero salia de la cuenta y el saldo pendiente
     * no se movia, porque el abono se insertaba como una cuota nueva ya pagada y
     * el saldo se calculaba sumando solo las cuotas NO pagadas.
     */
    protected open suspend fun amortizePending(
        debtId: String,
        amount: Double,
        paidDate: Long,
        sourceAccountId: String?,
        note: String?,
        skipInstallmentId: String? = null
    ): Double {
        var resto = amount
        val pendientes = getInstallmentsForDebt(debtId)
            .filter { !it.isPaid && it.id != skipInstallmentId }
            .sortedWith(compareBy({ it.dueDate }, { it.installmentNumber }))

        for (cuota in pendientes) {
            if (resto <= CurrencyFormatter.EPSILON) break
            val due = cuota.principalAmount + cuota.interestAmount

            if (resto >= due - CurrencyFormatter.EPSILON) {
                updateInstallment(
                    cuota.copy(
                        isPaid = true,
                        paidDate = paidDate,
                        actualPaidAmount = (cuota.actualPaidAmount ?: 0.0) + due,
                        paymentAccountId = sourceAccountId ?: cuota.paymentAccountId,
                        notes = note ?: cuota.notes
                    )
                )
                resto -= due
            } else {
                // Amortizacion parcial: primero interes, luego capital.
                val nuevoInteres = (cuota.interestAmount - resto).coerceAtLeast(0.0)
                val usadoEnInteres = cuota.interestAmount - nuevoInteres
                val nuevoCapital = (cuota.principalAmount - (resto - usadoEnInteres)).coerceAtLeast(0.0)
                updateInstallment(
                    cuota.copy(
                        principalAmount = nuevoCapital,
                        interestAmount = nuevoInteres,
                        actualPaidAmount = (cuota.actualPaidAmount ?: 0.0) + resto,
                        paidDate = paidDate,
                        paymentAccountId = sourceAccountId ?: cuota.paymentAccountId,
                        notes = note ?: cuota.notes
                    )
                )
                resto = 0.0
            }
        }
        return resto
    }

    /**
     * Saldo pendiente real. Misma definicion que DebtWithInstallments.remainingTotalDebt,
     * para que el estado persistido y el que ve la UI no puedan divergir.
     */
    protected open suspend fun computeRemaining(debt: DebtEntity): Double {
        val all = getInstallmentsForDebt(debt.id)
        val raw = if (debt.totalInstallments <= 0) {
            val desembolsado = all.sumOf {
                it.actualPaidAmount
                    ?: (if (it.isPaid) it.principalAmount + it.interestAmount + it.lateFeeAmount else 0.0)
            }
            val mora = all.sumOf { it.lateFeeAmount }
            debt.totalWithInterest - (desembolsado - mora).coerceAtLeast(0.0)
        } else {
            all.filter { !it.isPaid }.sumOf { it.principalAmount + it.interestAmount }
        }
        return if (raw <= CurrencyFormatter.EPSILON) 0.0 else raw
    }

    /**
     * Recalcula ACTIVE/PAID a partir del saldo real, con tolerancia de medio centavo.
     * Tambien reabre una deuda marcada como pagada si vuelve a tener saldo.
     */
    protected open suspend fun refreshDebtStatus(debt: DebtEntity) {
        val remaining = computeRemaining(debt)

        val nuevoEstado = when {
            remaining <= CurrencyFormatter.EPSILON -> DebtStatus.PAID
            debt.status == DebtStatus.PAID -> DebtStatus.ACTIVE
            else -> debt.status
        }
        if (nuevoEstado != debt.status) updateDebt(debt.copy(status = nuevoEstado))
    }

    /** Registra el egreso de caja asociado a un pago de deuda. */
    protected open suspend fun registrarEgreso(
        amount: Double,
        sourceAccountId: String?,
        paidDate: Long,
        note: String,
        accountDao: AccountDao,
        transactionDao: TransactionDao
    ) {
        if (sourceAccountId.isNullOrBlank() || amount <= CurrencyFormatter.EPSILON) return
        val account = accountDao.getAccountById(sourceAccountId) ?: return
        transactionDao.insertTransaction(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                type = TransactionType.EXPENSE,
                sourceAccountId = sourceAccountId,
                targetAccountId = null,
                amount = amount,
                categoryId = AppCategories.DEBT_PAYMENT,
                note = note,
                timestamp = paidDate,
                feeAmount = 0.0,
                targetAmount = null,
                targetCurrency = null,
                exchangeRate = null,
                installments = null,
                isSynced = false,
                syncTimestamp = null
            )
        )
        accountDao.updateBalance(sourceAccountId, account.currentBalance - amount)
    }

    // ------------------------------------------------------------------
    // Operaciones publicas
    // ------------------------------------------------------------------

    @Transaction
    open suspend fun payInstallmentAtomic(
        installmentId: String,
        paidDate: Long,
        actualAmount: Double,
        lateFee: Double,
        sourceAccountId: String?,
        notes: String?,
        accountDao: AccountDao,
        transactionDao: TransactionDao
    ) {
        val installment = getInstallmentById(installmentId) ?: return
        val debt = getDebtById(installment.debtId) ?: return

        // INV-D1: la mora se cobra primero y no amortiza.
        val amortizante = (actualAmount - lateFee).coerceAtLeast(0.0)
        val due = installment.principalAmount + installment.interestAmount
        val yaAbonado = installment.actualPaidAmount ?: 0.0

        if (amortizante >= due - CurrencyFormatter.EPSILON) {
            updateInstallment(
                installment.copy(
                    isPaid = true,
                    paidDate = paidDate,
                    actualPaidAmount = yaAbonado + amortizante + lateFee,
                    lateFeeAmount = installment.lateFeeAmount + lateFee,
                    paymentAccountId = sourceAccountId,
                    notes = notes ?: installment.notes
                )
            )
            // El excedente adelanta las cuotas siguientes en lugar de perderse.
            val sobrante = amortizante - due
            if (sobrante > CurrencyFormatter.EPSILON) {
                amortizePending(
                    debtId = debt.id,
                    amount = sobrante,
                    paidDate = paidDate,
                    sourceAccountId = sourceAccountId,
                    note = "Cubierta por excedente de la cuota " + installment.installmentNumber,
                    skipInstallmentId = installmentId
                )
            }
        } else {
            // PAGO PARCIAL: la cuota sigue pendiente por el saldo no cubierto.
            val nuevoInteres = (installment.interestAmount - amortizante).coerceAtLeast(0.0)
            val usadoEnInteres = installment.interestAmount - nuevoInteres
            val nuevoCapital = (installment.principalAmount - (amortizante - usadoEnInteres)).coerceAtLeast(0.0)
            updateInstallment(
                installment.copy(
                    principalAmount = nuevoCapital,
                    interestAmount = nuevoInteres,
                    lateFeeAmount = installment.lateFeeAmount + lateFee,
                    actualPaidAmount = yaAbonado + amortizante + lateFee,
                    paidDate = paidDate,
                    paymentAccountId = sourceAccountId,
                    notes = notes ?: "Pago parcial aplicado"
                )
            )
        }

        val noteDetail = "Pago cuota #${installment.installmentNumber}" +
                (if (debt.totalInstallments > 0) "/${debt.totalInstallments}" else "") +
                " de ${debt.entityName} (${debt.title})" +
                (if (lateFee > 0.0) " (Incluye mora: ${CurrencyFormatter.format(lateFee, debt.currency)})" else "")
        registrarEgreso(actualAmount, sourceAccountId, paidDate, noteDetail, accountDao, transactionDao)

        getDebtById(debt.id)?.let { refreshDebtStatus(it) }
    }

    @Transaction
    open suspend fun addDebtAbonoAtomic(
        debtId: String,
        abonoAmount: Double,
        paidDate: Long,
        sourceAccountId: String?,
        notes: String?,
        accountDao: AccountDao,
        transactionDao: TransactionDao
    ) {
        val debt = getDebtById(debtId) ?: return
        if (abonoAmount <= CurrencyFormatter.EPSILON) return

        // Nunca cobrar mas de lo que se debe: el excedente no se debita.
        val saldo = computeRemaining(debt)
        if (saldo <= 0.0) return
        val efectivo = if (abonoAmount > saldo) saldo else abonoAmount

        if (debt.totalInstallments <= 0) {
            // Abonos libres: el abono ES el registro del historial.
            val allInstallments = getInstallmentsForDebt(debtId)
            val nextNumber = (allInstallments.maxOfOrNull { it.installmentNumber } ?: 0) + 1
            insertInstallments(
                listOf(
                    DebtInstallmentEntity(
                        id = UUID.randomUUID().toString(),
                        debtId = debtId,
                        installmentNumber = nextNumber,
                        dueDate = paidDate,
                        principalAmount = efectivo,
                        interestAmount = 0.0,
                        lateFeeAmount = 0.0,
                        isPaid = true,
                        paidDate = paidDate,
                        actualPaidAmount = efectivo,
                        paymentAccountId = sourceAccountId,
                        notes = notes ?: "Abono a capital"
                    )
                )
            )
        } else {
            // Cuotas fijas: el abono amortiza el calendario existente.
            amortizePending(
                debtId = debtId,
                amount = efectivo,
                paidDate = paidDate,
                sourceAccountId = sourceAccountId,
                note = notes ?: "Cubierta por abono extraordinario"
            )
        }

        registrarEgreso(
            amount = efectivo,
            sourceAccountId = sourceAccountId,
            paidDate = paidDate,
            note = "Abono a deuda: ${debt.entityName}" + (notes?.let { " - $it" } ?: ""),
            accountDao = accountDao,
            transactionDao = transactionDao
        )

        getDebtById(debtId)?.let { refreshDebtStatus(it) }
    }
}
