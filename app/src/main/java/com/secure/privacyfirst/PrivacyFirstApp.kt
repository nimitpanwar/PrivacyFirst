package com.secure.privacyfirst

import android.app.Application
import android.util.Log
import com.secure.privacyfirst.network.RetrofitClient

/**
 * Application class to initialize components that need Context
 */
class PrivacyFirstApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        Log.d("PrivacyFirstApp", "Initializing application...")
        
        // Initialize RetrofitClient with cache directory
        RetrofitClient.init(this)
        
        Log.d("PrivacyFirstApp", "Application initialized successfully")
    }
}
