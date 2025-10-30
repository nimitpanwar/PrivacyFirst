package com.secure.privacyfirst.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.SslErrorHandler
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.secure.privacyfirst.SettingsActivity

private const val TAG = "WebViewScreen"

@Composable
fun WebViewScreen() {
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    // Handle back button press
    BackHandler(enabled = true) {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            // If can't go back in WebView, we're at the home page
            // You might want to show an exit dialog or just stay on the page
            Toast.makeText(context, "Already at home page", Toast.LENGTH_SHORT).show()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                // Configure global cookie manager first
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)
                
                WebView(ctx).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = true
                        displayZoomControls = false
                        setSupportZoom(true)
                        allowFileAccess = true
                        
                        // More permissive security settings
                        mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                        allowContentAccess = true
                        allowFileAccessFromFileURLs = false
                        allowUniversalAccessFromFileURLs = false
                        
                        // Enable necessary features for banking sites
                        setGeolocationEnabled(false)
                        databaseEnabled = true
                        javaScriptCanOpenWindowsAutomatically = true
                        
                        // Don't save sensitive data
                        saveFormData = false
                        savePassword = false
                        
                        // Enable caching
                        cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        
                        // Enhanced user agent
                        userAgentString = "Mozilla/5.0 (Linux; Android 13; Pixel 7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.6099.230 Mobile Safari/537.36"
                    }
                    
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    
                    // Enable cookies for this WebView instance
                    cookieManager.setAcceptThirdPartyCookies(this, true)
                    
                    // Add JavaScript interface
                    addJavascriptInterface(WebAppInterface(context), "Android")
                    
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val url = request?.url.toString()
                            
                            // Allow local file access
                            if (url.startsWith("file:///")) {
                                return false
                            }
                            
                            // Allow HTTP for redirects (some banks use this temporarily)
                            if (!url.startsWith("https://") && !url.startsWith("http://")) {
                                Toast.makeText(
                                    context,
                                    "Invalid URL protocol",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.w(TAG, "Blocked invalid URL: $url")
                                return true
                            }
                            
                            // Verify it's a banking domain
                            val uri = Uri.parse(url)
                            val host = uri.host?.lowercase() ?: ""
                            val allowedDomains = listOf(
                                "sbi.bank.in",
                                "onlinesbi.sbi",
                                "sbi.co.in",
                                "icicibank.com",
                                "infinity.icicibank.com",
                                "kotak.com",
                                "netbanking.kotak.com",
                                "yesbank.in",
                                "citibank.co.in",
                                "online.citibank.co.in",
                                "americanexpress.com",
                                "ucobank.com",
                                "indusind.com",
                                "hdfcbank.com",
                                "netbanking.hdfcbank.com"
                            )
                            
                            val isAllowed = allowedDomains.any { domain ->
                                host.contains(domain) || host.endsWith(domain)
                            }
                            
                            if (!isAllowed) {
                                Toast.makeText(
                                    context,
                                    "Access restricted to trusted banking sites",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.w(TAG, "Blocked non-whitelisted domain: $host")
                                return true
                            }
                            
                            Log.d(TAG, "Loading URL: $url")
                            // Load URL
                            view?.loadUrl(url)
                            return true
                        }
                        
                        override fun onReceivedSslError(
                            view: WebView?,
                            handler: SslErrorHandler?,
                            error: android.net.http.SslError?
                        ) {
                            val url = error?.url ?: ""
                            val errorType = when (error?.primaryError) {
                                android.net.http.SslError.SSL_NOTYETVALID -> "Certificate not yet valid"
                                android.net.http.SslError.SSL_EXPIRED -> "Certificate expired"
                                android.net.http.SslError.SSL_IDMISMATCH -> "Hostname mismatch"
                                android.net.http.SslError.SSL_UNTRUSTED -> "Certificate not trusted"
                                android.net.http.SslError.SSL_DATE_INVALID -> "Certificate date invalid"
                                android.net.http.SslError.SSL_INVALID -> "Certificate invalid"
                                else -> "Unknown SSL error"
                            }
                            
                            Log.w(TAG, "SSL Error on $url: $errorType - Proceeding anyway for banking site")
                            
                            // Always proceed for whitelisted domains
                            // This is necessary because some Indian banks have SSL config issues
                            handler?.proceed()
                            
                            Toast.makeText(
                                context,
                                "Connecting securely to banking site...",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        
                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?
                        ) {
                            super.onReceivedError(view, request, error)
                            val errorCode = error?.errorCode ?: 0
                            val description = error?.description?.toString() ?: "Unknown error"
                            val failingUrl = request?.url?.toString() ?: "Unknown URL"
                            
                            Log.e(TAG, "WebView error $errorCode: $description on $failingUrl")
                            
                            // Show user-friendly error message for main frame only
                            if (request?.isForMainFrame == true) {
                                when (errorCode) {
                                    -6 -> Log.e(TAG, "Connection closed - may retry automatically")
                                    -2 -> {
                                        Toast.makeText(
                                            context,
                                            "No internet connection",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    else -> {
                                        Toast.makeText(
                                            context,
                                            "Connection error. Retrying...",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                        
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            Log.d(TAG, "Page finished loading: $url")
                        }
                    }
                    
                    // Load custom HTML from assets
                    loadUrl("file:///android_asset/index.html")
                }.also { webView = it }
            },
            update = { view ->
                webView = view
            }
        )
    }
}

class WebAppInterface(private val context: android.content.Context) {
    @JavascriptInterface
    fun openSettings() {
        val intent = Intent(context, SettingsActivity::class.java)
        context.startActivity(intent)
    }
}
