package com.jaimeyaxchealmazanpardo.exchangerateapp.api.responses

import com.google.gson.annotations.SerializedName

data class CurrencyResponse(
    val amount: Double = 0.0,
    val base: String = "",
    @SerializedName("start_date")
    val startDate: String = "",
    @SerializedName("end_date")
    val endDate : String = "",
    val rates: Map<String, Rates> = mapOf()
)