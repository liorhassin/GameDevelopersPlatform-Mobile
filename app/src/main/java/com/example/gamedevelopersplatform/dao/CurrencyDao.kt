package com.example.gamedevelopersplatform.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.gamedevelopersplatform.entity.Currency

@Dao
interface CurrencyDao {
    @Query("SELECT currencyRate FROM currencies WHERE currencyName = :currencyName")
    fun getRateByName(currencyName: String): String

    @Query("SELECT currencyRate FROM currencies WHERE currencyId = :currencyId")
    fun getRateById(currencyId: String): String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currency: Currency)

    @Query("UPDATE currencies SET currencyRate = :currencyRate WHERE currencyName = :currencyName")
    fun update(currencyName: String, currencyRate: String)
}