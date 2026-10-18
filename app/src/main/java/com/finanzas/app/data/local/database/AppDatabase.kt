package com.finanzas.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.finanzas.app.data.local.converters.Converters
import com.finanzas.app.data.local.dao.AccountDao
import com.finanzas.app.data.local.dao.CategoryDao
import com.finanzas.app.data.local.dao.TransactionDao
import com.finanzas.app.data.local.dao.DebtDao
import com.finanzas.app.data.local.entity.AccountEntity
import com.finanzas.app.data.local.entity.CategoryEntity
import com.finanzas.app.data.local.entity.DebtEntity
import com.finanzas.app.data.local.entity.DebtInstallmentEntity
import com.finanzas.app.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        DebtEntity::class,
        DebtInstallmentEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun debtDao(): DebtDao

    companion object {
        private const val DATABASE_NAME = "finanzas_db.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                var dbInstance: AppDatabase? = null
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addCallback(InitialDataCallback { dbInstance!! })
                    .fallbackToDestructiveMigration()
                    .build()
                dbInstance = instance
                INSTANCE = instance
                instance
            }
        }
    }
}
