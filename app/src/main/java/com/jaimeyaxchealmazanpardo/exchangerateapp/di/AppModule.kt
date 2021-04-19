package com.jaimeyaxchealmazanpardo.exchangerateapp.di

import com.jaimeyaxchealmazanpardo.exchangerateapp.api.FrankfurterApi
import com.jaimeyaxchealmazanpardo.exchangerateapp.repositories.CurrencyRepository
import com.jaimeyaxchealmazanpardo.exchangerateapp.repositories.DefaultCurrencyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providesLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    @Singleton
    @Provides
    fun providesOkhttpClient(httpLoggingInterceptor: HttpLoggingInterceptor) = OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build()

    @Singleton
    @Provides
    fun provideFrackfurterApi(okHttpClient: OkHttpClient): FrankfurterApi = Retrofit.Builder()
        .baseUrl("https://www.frankfurter.app")
        .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
        .build()
        .create(FrankfurterApi::class.java)

    @Singleton
    @Provides
    fun providesCurrencyRepository(api : FrankfurterApi) : CurrencyRepository = DefaultCurrencyRepository(api)
}