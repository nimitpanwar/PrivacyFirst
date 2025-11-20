package com.secure.privacyfirst.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.secure.privacyfirst.viewmodel.PasswordViewModel
import kotlinx.coroutines.launch

@Composable
fun PinBox(
    value: String,
    onChange: (String) -> Unit,
    current: FocusRequester,
    next: FocusRequester?,
    previous: FocusRequester?
) {
    OutlinedTextField(
        value = value,
        onValueChange = { new ->
            val digit = new.take(1).filter { it.isDigit() }
            onChange(digit)
            
            if (digit.isNotEmpty()) {
                // Move forward when entering a digit
                next?.requestFocus()
            } else if (new.isEmpty() && value.isNotEmpty()) {
                // Backspace pressed - cleared current digit, move to previous
                previous?.requestFocus()
            }
        },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .size(60.dp)
            .focusRequester(current),
        shape = RoundedCornerShape(10.dp),
        textStyle = LocalTextStyle.current.copy(
            fontSize = 20.sp,
            textAlign = TextAlign.Center
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupPinScreen(
    onBackClick: () -> Unit,
    onPinSet: () -> Unit,
    viewModel: PasswordViewModel = viewModel()
) {
    var pin1 by remember { mutableStateOf("") }
    var pin2 by remember { mutableStateOf("") }
    var pin3 by remember { mutableStateOf("") }
    var pin4 by remember { mutableStateOf("") }
    
    var confirmPin1 by remember { mutableStateOf("") }
    var confirmPin2 by remember { mutableStateOf("") }
    var confirmPin3 by remember { mutableStateOf("") }
    var confirmPin4 by remember { mutableStateOf("") }
    
    val r1 = remember { FocusRequester() }
    val r2 = remember { FocusRequester() }
    val r3 = remember { FocusRequester() }
    val r4 = remember { FocusRequester() }
    val cr1 = remember { FocusRequester() }
    val cr2 = remember { FocusRequester() }
    val cr3 = remember { FocusRequester() }
    val cr4 = remember { FocusRequester() }
    
    var error by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isPinSet by viewModel.isPinSet.collectAsState()
    
    val pin = pin1 + pin2 + pin3 + pin4
    val confirmPin = confirmPin1 + confirmPin2 + confirmPin3 + confirmPin4
    
    LaunchedEffect(Unit) {
        // Check if PIN is already set
        if (isPinSet) {
            // Show change PIN flow
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isPinSet) "Change PIN" else "Setup PIN") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = if (isPinSet) "Change Your PIN" else "Create Your PIN",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Text(
                        text = "Enter a 4-digit PIN to secure your passwords",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Enter PIN",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PinBox(pin1, { pin1 = it; if (error.isNotEmpty()) error = "" }, r1, r2, null)
                        PinBox(pin2, { pin2 = it; if (error.isNotEmpty()) error = "" }, r2, r3, r1)
                        PinBox(pin3, { pin3 = it; if (error.isNotEmpty()) error = "" }, r3, r4, r2)
                        PinBox(pin4, { pin4 = it; if (error.isNotEmpty()) error = "" }, r4, cr1, r3)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Confirm PIN",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PinBox(confirmPin1, { confirmPin1 = it; if (error.isNotEmpty()) error = "" }, cr1, cr2, null)
                        PinBox(confirmPin2, { confirmPin2 = it; if (error.isNotEmpty()) error = "" }, cr2, cr3, cr1)
                        PinBox(confirmPin3, { confirmPin3 = it; if (error.isNotEmpty()) error = "" }, cr3, cr4, cr2)
                        PinBox(confirmPin4, { confirmPin4 = it; if (error.isNotEmpty()) error = "" }, cr4, null, cr3)
                    }
                    
                    if (error.isNotEmpty()) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = {
                            when {
                                pin.length != 4 -> {
                                    error = "PIN must be 4 digits"
                                }
                                confirmPin.length != 4 -> {
                                    error = "Please confirm your PIN"
                                }
                                pin != confirmPin -> {
                                    error = "PINs don't match"
                                }
                                else -> {
                                    isLoading = true
                                    scope.launch {
                                        viewModel.savePin(pin)
                                        isLoading = false
                                        onPinSet()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && pin.isNotEmpty() && confirmPin.isNotEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (isPinSet) "Update PIN" else "Set PIN")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚠️ Important",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Remember your PIN. It's required to access your saved passwords.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
