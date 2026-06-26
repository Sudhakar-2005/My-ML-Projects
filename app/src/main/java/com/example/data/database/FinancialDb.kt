package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- entities ---

@Entity(tableName = "holdings")
data class Holding(
    @PrimaryKey val symbol: String,
    val name: String,
    val quantity: Double,
    val avgBuyPrice: Double,
    val currentPrice: Double,
    val sector: String
)

@Entity(tableName = "watchlist")
data class WatchlistItem(
    @PrimaryKey val symbol: String,
    val name: String,
    val currentPrice: Double,
    val changePercent: Double,
    val rsi: Int = 45,
    val macd: String = "Neutral"
)

@Entity(tableName = "transactions")
data class TransactionHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val shares: Double,
    val price: Double,
    val type: String, // "BUY" or "SELL"
    val date: Long = System.currentTimeMillis()
)

@Entity(tableName = "subscription_state")
data class SubscriptionState(
    @PrimaryKey val id: Int = 1,
    val planType: String = "Free", // "Free", "Pro", "Elite"
    val country: String = "India",
    val currency: String = "₹",
    val priceText: String = "Free",
    val couponApplied: String? = null
)

// --- DAOs ---

@Dao
interface FinancialDao {
    // Holdings Queries
    @Query("SELECT * FROM holdings")
    fun getAllHoldings(): Flow<List<Holding>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHolding(holding: Holding)

    @Update
    suspend fun updateHolding(holding: Holding)

    @Delete
    suspend fun deleteHolding(holding: Holding)

    @Query("DELETE FROM holdings WHERE symbol = :symbol")
    suspend fun deleteHoldingBySymbol(symbol: String)

    @Query("SELECT * FROM holdings WHERE symbol = :symbol")
    suspend fun getHoldingBySymbol(symbol: String): Holding?

    // Watchlist Queries
    @Query("SELECT * FROM watchlist")
    fun getWatchlist(): Flow<List<WatchlistItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlistItem(item: WatchlistItem)

    @Delete
    suspend fun deleteWatchlistItem(item: WatchlistItem)

    @Query("DELETE FROM watchlist WHERE symbol = :symbol")
    suspend fun deleteWatchlistBySymbol(symbol: String)

    // Transactions Queries
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionHistory)

    // Subscription Queries
    @Query("SELECT * FROM subscription_state WHERE id = 1")
    fun getSubscriptionStateFlow(): Flow<SubscriptionState?>

    @Query("SELECT * FROM subscription_state WHERE id = 1")
    suspend fun getSubscriptionState(): SubscriptionState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptionState(state: SubscriptionState)
}

// --- database ---

@Database(
    entities = [Holding::class, WatchlistItem::class, TransactionHistory::class, SubscriptionState::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun financialDao(): FinancialDao
}
