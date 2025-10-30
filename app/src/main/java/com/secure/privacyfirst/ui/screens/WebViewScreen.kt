package com.secure.privacyfirst.ui.screens

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun WebViewScreen() {
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
