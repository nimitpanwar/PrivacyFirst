package com.secure.privacyfirst.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.secure.privacyfirst.navigation.Screen
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

/**
 * Simple authentication screen placeholder.
 * - Provides a "Use biometrics" action (placeholder) and a 4-digit PIN entry.
 * - Calls onAuthenticated() when either method succeeds.
 */
@Composable
fun AuthScreen(
    navController: NavHostController? = null,
    onAuthenticated: () -> Unit = {}
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var pin by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Authenticate",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Biometrics placeholder
        Button(
            onClick = {
                        // Real biometric flow using BiometricPrompt
                        val activity = context as? FragmentActivity
                        if (activity == null) {
                            Toast.makeText(context, "Biometric unavailable", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val executor = ContextCompat.getMainExecutor(context)
                        val biometricPrompt = BiometricPrompt(activity, executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    super.onAuthenticationSucceeded(result)
                                    Toast.makeText(context, "Authenticated", Toast.LENGTH_SHORT).show()
                                    if (navController != null) {
                                        navController.navigate(Screen.WebView.route) {
                                            popUpTo(Screen.Auth.route) { inclusive = true }
                                        }
                                    } else {
                                        onAuthenticated()
                                    }
                                }

                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                    super.onAuthenticationError(errorCode, errString)
                                    // Show the error to the user
                                    Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
                                }

                                override fun onAuthenticationFailed() {
                                    super.onAuthenticationFailed()
                                    Toast.makeText(context, "Authentication failed", Toast.LENGTH_SHORT).show()
                                }
                            })

                        val biometricManager = BiometricManager.from(context)
                        if (biometricManager.canAuthenticate() != BiometricManager.BIOMETRIC_SUCCESS) {
                            Toast.makeText(context, "No biometrics enrolled or not available", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Authenticate")
                            .setSubtitle("Use fingerprint or face to continue")
                            .setNegativeButtonText("Use PIN")
                            .build()

                        biometricPrompt.authenticate(promptInfo)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = "Use Biometric (Face/Touch)", color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Or enter your 4-digit PIN", fontSize = 14.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f))

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = pin,
            onValueChange = { new ->
                // accept only digits and limit length to 4
                val filtered = new.filter { it.isDigit() }.take(4)
                pin = filtered
                if (error.isNotEmpty()) error = ""
            },
            placeholder = { Text("••••") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (error.isNotEmpty()) {
            Text(text = error, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                focusManager.clearFocus()
                if (pin.length == 4) {
                    // In a real app validate the PIN securely (call into auth storage / server)
                    if (navController != null) {
                        Toast.makeText(context, "PIN accepted", Toast.LENGTH_SHORT).show()
                        navController.navigate(Screen.WebView.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    } else {
                        onAuthenticated()
                    }
                } else {
                    error = "Enter a 4-digit PIN"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = pin.isNotEmpty()
        ) {
            Text(text = "Unlock")
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { /* Optional: show forgot PIN flow */ }) {
            Text(text = "Forgot PIN?", color = MaterialTheme.colorScheme.primary)
        }
    }
}
