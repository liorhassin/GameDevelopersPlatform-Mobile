package com.example.gamedevelopersplatform.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "currencies")
data class Currency(
    @PrimaryKey var currencyId: String,
    @ColumnInfo(name = "currencyName") var currencyName: String?,
    @ColumnInfo(name = "currencyRate") var currencyRate: String?,
){
    constructor(): this("", null, null)
}
