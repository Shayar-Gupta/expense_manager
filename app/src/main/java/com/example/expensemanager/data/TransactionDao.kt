package com.example.expensemanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE messageId = :id)")
    suspend fun exists(id: String): Boolean

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun streamAll(): Flow<List<TransactionEntity>>
}
