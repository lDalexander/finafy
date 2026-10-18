package com.finanzas.app.data.repository

import com.finanzas.app.data.local.dao.CategoryDao
import com.finanzas.app.data.mapper.toDomain
import com.finanzas.app.data.mapper.toEntity
import com.finanzas.app.domain.model.Category
import com.finanzas.app.domain.model.TransactionType
import com.finanzas.app.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getAllCategoriesFlow(): Flow<List<Category>> {
        return categoryDao.getAllCategoriesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getCategoriesByTypeFlow(type: TransactionType): Flow<List<Category>> {
        return categoryDao.getCategoriesByTypeFlow(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoriesByType(type: TransactionType): List<Category> {
        return categoryDao.getCategoriesByType(type).map { it.toDomain() }
    }

    override suspend fun getCategoryById(id: String): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun insertCategories(categories: List<Category>) {
        categoryDao.insertCategories(categories.map { it.toEntity() })
    }

    override suspend fun deleteCategory(categoryId: String) {
        categoryDao.deleteCategoryById(categoryId)
    }
}
