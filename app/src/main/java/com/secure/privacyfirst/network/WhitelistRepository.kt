package com.secure.privacyfirst.network

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WhitelistRepository(private val context: Context) {
    
    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(context)
    
    // In-memory cache for whitelist
    private var cachedWhitelist: List<String>? = null
    private var cacheTimestamp: Long = 0
    private val cacheDuration = 5 * 60 * 1000L // 5 minutes
    
    companion object {
        private const val TAG = "WhitelistRepository"
        private const val ADMIN_USERNAME = "admin"
        // Updated password to match API documentation
        private const val ADMIN_PASSWORD = "Byf8\$G&F*G8vGEfuhfuhfEHU!89f2qfiHT88%ffyutf7^s"
        private const val MAX_RETRY_ATTEMPTS = 3
        private const val INITIAL_RETRY_DELAY = 1000L // 1 second
    }
    
    /**
     * Login and store JWT token
     */
    suspend fun login(username: String = ADMIN_USERNAME, password: String = ADMIN_PASSWORD): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    tokenManager.saveToken(loginResponse.token, loginResponse.expiresIn)
                    Log.d(TAG, "Login successful, token saved")
                    Result.success(loginResponse.token)
                } else {
                    val error = "Login failed: ${response.code()}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get whitelist URLs from server with caching and retry logic
     */
    suspend fun getWhitelist(forceRefresh: Boolean = false): Result<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                // Check in-memory cache first
                if (!forceRefresh && cachedWhitelist != null && 
                    System.currentTimeMillis() - cacheTimestamp < cacheDuration) {
                    Log.d(TAG, "Returning cached whitelist: ${cachedWhitelist!!.size} URLs")
                    return@withContext Result.success(cachedWhitelist!!)
                }
                
                // Check if token is valid, if not login first
                if (!tokenManager.isTokenValid()) {
                    Log.d(TAG, "Token expired or missing, logging in...")
                    val loginResult = loginWithRetry()
                    if (loginResult.isFailure) {
                        return@withContext Result.failure(loginResult.exceptionOrNull() ?: Exception("Login failed"))
                    }
                }
                
                val authHeader = tokenManager.getAuthHeader()
                if (authHeader == null) {
                    Log.e(TAG, "No auth token available")
                    return@withContext Result.failure(Exception("No authentication token"))
                }
                
                // Fetch with retry logic
                val response = retryApiCall { apiService.getWhitelist(authHeader) }
                
                if (response.isSuccessful && response.body() != null) {
                    val urls = response.body()!!.urls
                    // Update cache
                    cachedWhitelist = urls
                    cacheTimestamp = System.currentTimeMillis()
                    Log.d(TAG, "Whitelist fetched successfully: ${urls.size} URLs")
                    Result.success(urls)
                } else if (response.code() == 401) {
                    // Token might be invalid, try re-login and retry
                    Log.d(TAG, "Received 401, attempting re-login...")
                    val loginResult = loginWithRetry()
                    if (loginResult.isSuccess) {
                        val newAuthHeader = tokenManager.getAuthHeader()!!
                        val retryResponse = apiService.getWhitelist(newAuthHeader)
                        if (retryResponse.isSuccessful && retryResponse.body() != null) {
                            val urls = retryResponse.body()!!.urls
                            cachedWhitelist = urls
                            cacheTimestamp = System.currentTimeMillis()
                            Log.d(TAG, "Whitelist fetched after re-login: ${urls.size} URLs")
                            return@withContext Result.success(urls)
                        }
                    }
                    Result.failure(Exception("Authentication failed"))
                } else {
                    val error = "Failed to fetch whitelist: ${response.code()}"
                    Log.e(TAG, error)
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching whitelist: ${e.message}", e)
                // Return cached data if available even on error
                if (cachedWhitelist != null) {
                    Log.d(TAG, "Returning stale cache due to error")
                    Result.success(cachedWhitelist!!)
                } else {
                    Result.failure(e)
                }
            }
        }
    }
    
    /**
     * Login with exponential backoff retry
     */
    private suspend fun loginWithRetry(): Result<String> {
        var lastException: Exception? = null
        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            try {
                val result = login()
                if (result.isSuccess) {
                    return result
                }
                lastException = result.exceptionOrNull() as? Exception
            } catch (e: Exception) {
                lastException = e
            }
            
            if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                val delay = INITIAL_RETRY_DELAY * (1 shl attempt) // Exponential backoff
                Log.d(TAG, "Login attempt ${attempt + 1} failed, retrying in ${delay}ms...")
                kotlinx.coroutines.delay(delay)
            }
        }
        return Result.failure(lastException ?: Exception("Login failed after $MAX_RETRY_ATTEMPTS attempts"))
    }
    
    /**
     * Generic retry logic for API calls
     */
    private suspend fun <T> retryApiCall(
        apiCall: suspend () -> retrofit2.Response<T>
    ): retrofit2.Response<T> {
        var lastException: Exception? = null
        repeat(MAX_RETRY_ATTEMPTS) { attempt ->
            try {
                return apiCall()
            } catch (e: Exception) {
                lastException = e
                if (attempt < MAX_RETRY_ATTEMPTS - 1) {
                    val delay = INITIAL_RETRY_DELAY * (1 shl attempt)
                    Log.d(TAG, "API call attempt ${attempt + 1} failed, retrying in ${delay}ms...")
                    kotlinx.coroutines.delay(delay)
                }
            }
        }
        throw lastException ?: Exception("API call failed after $MAX_RETRY_ATTEMPTS attempts")
    }
    
    /**
     * Clear the in-memory cache
     */
    fun clearCache() {
        cachedWhitelist = null
        cacheTimestamp = 0
        Log.d(TAG, "Cache cleared")
    }
    
    /**
     * Add URL to whitelist
     */
    suspend fun addUrl(url: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (!tokenManager.isTokenValid()) {
                    login()
                }
                
                val authHeader = tokenManager.getAuthHeader() ?: return@withContext Result.failure(
                    Exception("No authentication token")
                )
                
                val response = apiService.addUrl(authHeader, AddUrlRequest(url))
                if (response.isSuccessful) {
                    Log.d(TAG, "URL added successfully: $url")
                    Result.success(response.body()?.message ?: "URL added")
                } else {
                    Result.failure(Exception("Failed to add URL: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error adding URL: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Update URL in whitelist
     */
    suspend fun updateUrl(oldUrl: String, newUrl: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (!tokenManager.isTokenValid()) {
                    login()
                }
                
                val authHeader = tokenManager.getAuthHeader() ?: return@withContext Result.failure(
                    Exception("No authentication token")
                )
                
                val response = apiService.updateUrl(authHeader, UpdateUrlRequest(oldUrl, newUrl))
                if (response.isSuccessful) {
                    Log.d(TAG, "URL updated successfully")
                    Result.success(response.body()?.message ?: "URL updated")
                } else {
                    Result.failure(Exception("Failed to update URL: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating URL: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Delete URL from whitelist
     */
    suspend fun deleteUrl(url: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                if (!tokenManager.isTokenValid()) {
                    login()
                }
                
                val authHeader = tokenManager.getAuthHeader() ?: return@withContext Result.failure(
                    Exception("No authentication token")
                )
                
                val response = apiService.deleteUrl(authHeader, DeleteUrlRequest(url))
                if (response.isSuccessful) {
                    Log.d(TAG, "URL deleted successfully: $url")
                    Result.success(response.body()?.message ?: "URL deleted")
                } else {
                    Result.failure(Exception("Failed to delete URL: ${response.code()}"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting URL: ${e.message}", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Logout and clear token
     */
    suspend fun logout() {
        withContext(Dispatchers.IO) {
            tokenManager.clearToken()
            Log.d(TAG, "Logged out, token cleared")
        }
    }
}
