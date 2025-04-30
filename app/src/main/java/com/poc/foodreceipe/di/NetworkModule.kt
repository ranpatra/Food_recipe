
package com.poc.foodreceipe.di


import android.app.Application
import android.content.Context
import com.poc.foodreceipe.FoodReceipeApp
import com.poc.foodreceipe.data.network.ApiService
import com.poc.foodreceipe.utils.AppConstants
import com.poc.foodreceipe.utils.CryptoHelper
import com.poc.foodreceipe.utils.NetworkChecker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providesHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        val cryptoHelper = (context.applicationContext as FoodReceipeApp).cryptoHelper

        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val originalHttpUrl = original.url

                // Get encrypted API key
                val apiKey = cryptoHelper.getApiKey() ?: throw IllegalStateException("API key not configured")

                val url = originalHttpUrl.newBuilder()
                    .addQueryParameter("apiKey", apiKey)
                    .build()

                val requestBuilder = original.newBuilder().url(url)
                chain.proceed(requestBuilder.build())
            }
            .build()
    }


    @Provides
    fun providesConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }


    @Singleton
    @Provides
    fun provideRetrofitInstance(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AppConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Singleton
    @Provides
    fun provideApiInstance(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }
}


@Module
@InstallIn(SingletonComponent::class)
object NetworkCheckerModule {

    @Provides
    fun provideNetworkChecker(@ApplicationContext context: Context): NetworkChecker {
        return NetworkChecker(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideContext(application: Application): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
        fun provideCryptoHelper(
            @ApplicationContext context: Context
        ): CryptoHelper = CryptoHelper(context)
}