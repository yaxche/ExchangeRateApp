package com.jaimeyaxchealmazanpardo.exchangerateapp.api


import com.jaimeyaxchealmazanpardo.exchangerateapp.api.responses.CurrencyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FrankfurterApi {

    @GET("/{date}")
    suspend fun getRates(@Path("date") date : String,@Query("from") base :String, @Query("to") target : String) : Response<CurrencyResponse>
}