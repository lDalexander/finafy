package com.finanzas.app.data.local.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.finanzas.app.data.local.entity.AccountEntity
import com.finanzas.app.core.util.AppCategories
import com.finanzas.app.data.local.entity.CategoryEntity
import com.finanzas.app.domain.model.AccountType
import com.finanzas.app.domain.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class InitialDataCallback(
    private val databaseProvider: () -> AppDatabase
) : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            populateDatabase()
        }
    }

    override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
        super.onDestructiveMigration(db)
        CoroutineScope(Dispatchers.IO).launch {
            populateDatabase()
        }
    }

    /**
     * Bases ya existentes no pasan por onCreate. Aqui se garantiza que la
     * categoria reservada de pagos de deuda exista, sin tocar el esquema
     * (la base usa fallbackToDestructiveMigration: subir la version borraria datos).
     */
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        CoroutineScope(Dispatchers.IO).launch {
            val categoryDao = databaseProvider().categoryDao()
            if (categoryDao.getCategoryById(AppCategories.DEBT_PAYMENT) == null) {
                categoryDao.insertCategory(
                    CategoryEntity(
                        AppCategories.DEBT_PAYMENT,
                        "Pago de Deudas",
                        "account_balance",
                        TransactionType.EXPENSE,
                        "#F59E0B"
                    )
                )
            }
        }
    }

    private suspend fun populateDatabase() {
        val database = databaseProvider()
        val accountDao = database.accountDao()
        val categoryDao = database.categoryDao()

        // 1. Única Cuenta Inicial por Defecto: Billetera Efectivo (El usuario puede agregar más cuentas libremente)
        val defaultAccounts = listOf(
            AccountEntity(
                id = "acc_cash_01",
                name = "Billetera Efectivo",
                type = AccountType.CASH,
                currency = "USD",
                currentBalance = 0.0,
                colorHex = "#10B981", // Emerald
                isDefault = true
            )
        )
        accountDao.insertAccounts(defaultAccounts)

        // 2. Categorías Iniciales
        val defaultCategories = listOf(
            // Gastos
            CategoryEntity("cat_exp_food", "Alimentación", "restaurant", TransactionType.EXPENSE, "#F59E0B"),
            CategoryEntity("cat_exp_trans", "Transporte", "directions_car", TransactionType.EXPENSE, "#3B82F6"),
            CategoryEntity("cat_exp_market", "Supermercado", "shopping_cart", TransactionType.EXPENSE, "#10B981"),
            CategoryEntity("cat_exp_home", "Servicios & Hogar", "home", TransactionType.EXPENSE, "#6366F1"),
            CategoryEntity("cat_exp_fun", "Entretenimiento", "movie", TransactionType.EXPENSE, "#EC4899"),
            CategoryEntity("cat_exp_health", "Salud & Farmacia", "medical_services", TransactionType.EXPENSE, "#EF4444"),
            CategoryEntity("cat_exp_edu", "Educación", "school", TransactionType.EXPENSE, "#8B5CF6"),
            CategoryEntity("cat_exp_other", "Otros Gastos", "category", TransactionType.EXPENSE, "#64748B"),
            CategoryEntity(AppCategories.DEBT_PAYMENT, "Pago de Deudas", "account_balance", TransactionType.EXPENSE, "#F59E0B"),

            // Tarjeta de Crédito
            CategoryEntity("cat_cc_online", "Compras Online", "shopping_bag", TransactionType.CREDIT_PURCHASE, "#EC4899"),
            CategoryEntity("cat_cc_dining", "Restaurantes", "restaurant", TransactionType.CREDIT_PURCHASE, "#F59E0B"),
            CategoryEntity("cat_cc_travel", "Viajes & Vuelos", "flight", TransactionType.CREDIT_PURCHASE, "#06B6D4"),
            CategoryEntity("cat_cc_tech", "Tecnología", "devices", TransactionType.CREDIT_PURCHASE, "#3B82F6"),
            CategoryEntity("cat_cc_subs", "Suscripciones", "subscriptions", TransactionType.CREDIT_PURCHASE, "#8B5CF6"),

            // Remesas / Giros
            CategoryEntity("cat_rem_colombia", "Giro Familiar Colombia", "send", TransactionType.REMITTANCE, "#10B981"),
            CategoryEntity("cat_rem_savings", "Remesa Ahorro", "savings", TransactionType.REMITTANCE, "#3B82F6"),
            CategoryEntity("cat_rem_bills", "Pago Exterior", "payments", TransactionType.REMITTANCE, "#F59E0B"),

            // Ingresos (Múltiples tipos de ingresos configurables)
            CategoryEntity("cat_inc_salary", "Salario / Nómina", "payments", TransactionType.INCOME, "#10B981"),
            CategoryEntity("cat_inc_invest", "Inversiones & Dividendos", "trending_up", TransactionType.INCOME, "#8B5CF6"),
            CategoryEntity("cat_inc_freelance", "Otros Trabajos / Freelance", "work", TransactionType.INCOME, "#F59E0B"),
            CategoryEntity("cat_inc_sales", "Negocio / Ventas", "store", TransactionType.INCOME, "#3B82F6"),
            CategoryEntity("cat_inc_returns", "Rendimientos Financieros", "account_balance", TransactionType.INCOME, "#06B6D4"),
            CategoryEntity("cat_inc_refund", "Reembolsos", "receipt_long", TransactionType.INCOME, "#EC4899"),
            CategoryEntity("cat_inc_other", "Otros Ingresos", "category", TransactionType.INCOME, "#64748B")
        )
        categoryDao.insertCategories(defaultCategories)
    }
}
