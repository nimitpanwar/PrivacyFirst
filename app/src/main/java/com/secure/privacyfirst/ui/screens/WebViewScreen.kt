package com.secure.privacyfirst.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding() // Add padding for status bar
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        builtInZoomControls = false
                        displayZoomControls = false
                        setSupportZoom(false)
                    }
                    
                    // Set transparent background for WebView
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    
                    webViewClient = WebViewClient()
                    
                    // Load custom HTML from assets
                    loadUrl("file:///android_asset/index.html")
                }
            },
            update = { webView ->
                // You can update the WebView here if needed
            }
        )
    }
}
