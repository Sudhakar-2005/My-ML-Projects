package com.example.data.repository

import com.example.data.database.FinancialDao
import com.example.data.database.Holding
import com.example.data.database.WatchlistItem
import com.example.data.database.TransactionHistory
import com.example.data.database.SubscriptionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import android.util.Log

class FinancialRepository(private val dao: FinancialDao) {

    val allHoldings: Flow<List<Holding>> = dao.getAllHoldings()
    val watchlist: Flow<List<WatchlistItem>> = dao.getWatchlist()
    val allTransactions: Flow<List<TransactionHistory>> = dao.getAllTransactions()
    val subscriptionState: Flow<SubscriptionState?> = dao.getSubscriptionStateFlow()

    suspend fun checkAndPopulateMockData() {
        try {
            // Check if holdings are empty
            val holdingsList = dao.getAllHoldings().first()
            if (holdingsList.isEmpty()) {
                Log.d("FinancialRepository", "Pre-populating mock holdings...")
                dao.insertHolding(Holding("TCS", "Tata Consultancy Services Ltd.", 15.0, 4000.0, 4210.0, "Technology"))
                dao.insertHolding(Holding("BEL", "Bharat Electronics Ltd.", 180.0, 260.0, 315.0, "Defense"))
                dao.insertHolding(Holding("HDFC", "HDFC Bank Ltd.", 25.0, 1550.0, 1620.0, "Financials"))
                dao.insertHolding(Holding("RELIANCE", "Reliance Industries Ltd.", 12.0, 2350.0, 2460.0, "Energy"))

                // Log pre-populated transactions
                dao.insertTransaction(TransactionHistory(symbol = "TCS", shares = 15.0, price = 4000.0, type = "BUY", date = System.currentTimeMillis() - 86400000 * 5))
                dao.insertTransaction(TransactionHistory(symbol = "BEL", shares = 180.0, price = 260.0, type = "BUY", date = System.currentTimeMillis() - 86400000 * 10))
                dao.insertTransaction(TransactionHistory(symbol = "HDFC", shares = 25.0, price = 1550.0, type = "BUY", date = System.currentTimeMillis() - 86400000 * 2))
                dao.insertTransaction(TransactionHistory(symbol = "RELIANCE", shares = 12.0, price = 2350.0, type = "BUY", date = System.currentTimeMillis() - 86400000 * 1))
            }

            // Check if watchlist is empty
            val watchlistList = dao.getWatchlist().first()
            if (watchlistList.isEmpty()) {
                Log.d("FinancialRepository", "Pre-populating watchlist...")
                dao.insertWatchlistItem(WatchlistItem("TCS", "Tata Consultancy Services Ltd.", 4210.0, 1.45, 28, "Bearish MACD crossover")) // RSI under 30 example
                dao.insertWatchlistItem(WatchlistItem("BEL", "Bharat Electronics Ltd.", 315.0, 4.20, 72, "Strong Volume Breakout"))
                dao.insertWatchlistItem(WatchlistItem("INFY", "Infosys Ltd.", 1820.0, -0.80, 45, "Neutral Signal"))
                dao.insertWatchlistItem(WatchlistItem("ICICI", "ICICI Bank Ltd.", 1115.0, -1.95, 34, "Oversold warning near support"))
                dao.insertWatchlistItem(WatchlistItem("ITC", "ITC Ltd.", 440.0, 0.40, 52, "Steady Consolidation"))
            }

            // Check subscription state
            val currentSub = dao.getSubscriptionState()
            if (currentSub == null) {
                dao.insertSubscriptionState(SubscriptionState(id = 1, planType = "Free", country = "India", currency = "₹", priceText = "Free"))
            }
        } catch (e: Exception) {
            Log.e("FinancialRepository", "Error prepopulating mock data", e)
        }
    }

    suspend fun buyStock(symbol: String, name: String, qty: Double, price: Double, sector: String) {
        val holding = dao.getHoldingBySymbol(symbol)
        if (holding == null) {
            dao.insertHolding(Holding(symbol, name, qty, price, price, sector))
        } else {
            val totalQty = holding.quantity + qty
            val totalCost = (holding.quantity * holding.avgBuyPrice) + (qty * price)
            val avgPrice = totalCost / totalQty
            dao.insertHolding(Holding(symbol, name, totalQty, avgPrice, price, sector))
        }
        dao.insertTransaction(TransactionHistory(symbol = symbol, shares = qty, price = price, type = "BUY"))
    }

    suspend fun sellStock(symbol: String, qty: Double, price: Double): Boolean {
        val holding = dao.getHoldingBySymbol(symbol) ?: return false
        if (holding.quantity < qty) return false

        val remainingQty = holding.quantity - qty
        if (remainingQty <= 0.0) {
            dao.deleteHoldingBySymbol(symbol)
        } else {
            dao.insertHolding(holding.copy(quantity = remainingQty, currentPrice = price))
        }
        dao.insertTransaction(TransactionHistory(symbol = symbol, shares = qty, price = price, type = "SELL"))
        return true
    }

    suspend fun updateSubscription(planType: String, country: String, currency: String, priceText: String, coupon: String? = null) {
        dao.insertSubscriptionState(SubscriptionState(id = 1, planType = planType, country = country, currency = currency, priceText = priceText, couponApplied = coupon))
    }

    suspend fun deleteWatchlist(symbol: String) {
        dao.deleteWatchlistBySymbol(symbol)
    }

    suspend fun addWatchlist(item: WatchlistItem) {
        dao.insertWatchlistItem(item)
    }
}
