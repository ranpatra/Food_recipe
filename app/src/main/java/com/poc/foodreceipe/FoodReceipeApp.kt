package com.poc.foodreceipe

import android.app.Application
import com.poc.foodreceipe.utils.CryptoHelper
import com.squareup.leakcanary.core.BuildConfig
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class FoodReceipeApp: Application(){
    private val _API_Key = "fbfd1247cef44d0381e056bf8ebc6ea3"
   // private val _API_Key = "c499c47f57f94f97a57350f0d2d8f9d4"
    lateinit var cryptoHelper: CryptoHelper
    override fun onCreate() {
        super.onCreate()
        cryptoHelper = CryptoHelper(this)
        if (cryptoHelper.getApiKey().isNullOrEmpty()) {
            cryptoHelper.storeApiKey(_API_Key)
        }
    }
}