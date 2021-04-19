package com.jaimeyaxchealmazanpardo.exchangerateapp.repositories

import com.jaimeyaxchealmazanpardo.exchangerateapp.api.FrankfurterApi
import com.jaimeyaxchealmazanpardo.exchangerateapp.api.responses.CurrencyResponse
import javax.inject.Inject


class DefaultCurrencyRepository @Inject constructor( private val api : FrankfurterApi) : CurrencyRepository  {

    private val TAG = "Repository"

    override suspend fun getCurrency(date: String, base : String, target : String) : CurrencyResponse? {
        val response = api.getRates(date, base, target)
        if (response.isSuccessful){
            return response.body()
        }else{
            throw Exception()
        }
    }
}
