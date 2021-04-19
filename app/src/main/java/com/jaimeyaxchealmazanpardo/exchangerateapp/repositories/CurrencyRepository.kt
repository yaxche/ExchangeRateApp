package com.jaimeyaxchealmazanpardo.exchangerateapp.repositories

import com.jaimeyaxchealmazanpardo.exchangerateapp.api.responses.CurrencyResponse

interface CurrencyRepository {

    suspend fun getCurrency(date : String,base : String = "EUR", target : String = "USD") : CurrencyResponse?

}