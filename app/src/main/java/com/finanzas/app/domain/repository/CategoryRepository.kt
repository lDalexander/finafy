package com.finanzas.app.domain.repository

import com.finanzas.app.domain.model.Category
import com.finanzas.app.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getAllCategoriesFlow(): Flow<List<Category>>
    fun getCategoriesByTypeFlow(type: TransactionType): Flow<List<Category>>
    suspend fun getCategoriesByType(type: TransactionType): List<Category>
    suspend fun getCategoryById(id: String): Category?
    suspend fun insertCategory(category: Category)
    suspend fun insertCategories(categories: List<Category>)
    suspend fun deleteCategory(categoryId: String)
}
