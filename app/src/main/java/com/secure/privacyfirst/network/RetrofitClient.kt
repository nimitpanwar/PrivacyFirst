package com.secure.privacyfirst.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import android.content.Context

object RetrofitClient {
    
    // Server IP - updated to match provided documentation
    private const val BASE_URL = "http://192.168.2.244:5001/"
    
    // Cache size: 10 MB
    private const val CACHE_SIZE = 10 * 1024 * 1024L
    
    private var cacheDir: File? = null
    
    /**
     * Initialize the cache directory. Call this from Application context.
     */
    fun init(context: Context) {
        cacheDir = File(context.cacheDir, "http_cache")
    }
    
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    /**
     * Cache interceptor for GET requests
     */
    private val cacheInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val cacheControl = CacheControl.Builder()
            .maxAge(5, TimeUnit.MINUTES) // Cache whitelist for 5 minutes
            .build()
        response.newBuilder()
            .header("Cache-Control", cacheControl.toString())
            .build()
    }
    
    /**
     * Offline cache interceptor
     */
    private val offlineCacheInterceptor = Interceptor { chain ->
        var request = chain.request()
        if (request.method == "GET") {
            val cacheControl = CacheControl.Builder()
                .maxStale(7, TimeUnit.DAYS) // Serve stale cache for up to 7 days if offline
                .build()
            request = request.newBuilder()
                .cacheControl(cacheControl)
                .build()
        }
        chain.proceed(request)
    }
    
    private val okHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addNetworkInterceptor(cacheInterceptor)
            .addInterceptor(offlineCacheInterceptor)
            .apply {
                cacheDir?.let {
                    cache(Cache(it, CACHE_SIZE))
                }
            }
            .connectTimeout(15, TimeUnit.SECONDS) // Reduced from 30s for faster failure detection
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true) // Auto retry on connection failure
            .build()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
