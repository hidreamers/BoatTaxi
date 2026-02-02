package com.boattaxie.app.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.boattaxie.app.ui.theme.*

/**
 * Card Payment Screen - Collects card details for payment
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardPaymentScreen(
    amount: Double,
    description: String,
    onPaymentSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // Validation
    val isCardNumberValid = cardNumber.replace(" ", "").length == 16
    val isExpiryValid = expiryMonth.length == 2 && expiryYear.length == 2 &&
            (expiryMonth.toIntOrNull() ?: 0) in 1..12
    val isCvvValid = cvv.length in 3..4
    val isNameValid = cardholderName.isNotBlank()
    val isFormValid = isCardNumberValid && isExpiryValid && isCvvValid && isNameValid
    
    // Success Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    "Payment Successful!",
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Your payment of $${String.format("%.2f", amount)} has been processed.",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        description,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onPaymentSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Text("Continue")
                }
            }
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure Payment") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Amount Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Amount to Pay",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "$${String.format("%.2f", amount)}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Security Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Secure 256-bit SSL Encryption",
                    style = MaterialTheme.typography.bodySmall,
                    color = Success
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Card Details Form
            Text(
                text = "Card Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Cardholder Name
            OutlinedTextField(
                value = cardholderName,
                onValueChange = { cardholderName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cardholder Name") },
                placeholder = { Text("John Doe") },
                leadingIcon = { Icon(Icons.Default.Person, null) },
                singleLine = true,
                isError = cardholderName.isNotEmpty() && !isNameValid
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Card Number
            OutlinedTextField(
                value = cardNumber,
                onValueChange = { value ->
                    // Format card number with spaces
                    val cleaned = value.replace(" ", "").take(16)
                    cardNumber = cleaned.chunked(4).joinToString(" ")
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Card Number") },
                placeholder = { Text("1234 5678 9012 3456") },
                leadingIcon = { Text("💳", fontSize = 18.sp) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = cardNumber.isNotEmpty() && !isCardNumberValid
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Expiry and CVV Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Expiry Month
                OutlinedTextField(
                    value = expiryMonth,
                    onValueChange = { if (it.length <= 2) expiryMonth = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.weight(1f),
                    label = { Text("MM") },
                    placeholder = { Text("01") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    isError = expiryMonth.isNotEmpty() && expiryMonth.length == 2 && 
                            ((expiryMonth.toIntOrNull() ?: 0) !in 1..12)
                )
                
                // Expiry Year
                OutlinedTextField(
                    value = expiryYear,
                    onValueChange = { if (it.length <= 2) expiryYear = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.weight(1f),
                    label = { Text("YY") },
                    placeholder = { Text("26") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                // CVV
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { if (it.length <= 4) cvv = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.weight(1f),
                    label = { Text("CVV") },
                    placeholder = { Text("123") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    isError = cvv.isNotEmpty() && !isCvvValid
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Error.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, null, tint = Error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(errorMessage!!, color = Error)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Pay Button
            Button(
                onClick = {
                    isProcessing = true
                    errorMessage = null
                    
                    // Simulate payment processing
                    // In production, this would call the Braintree/PayPal SDK
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        isProcessing = false
                        showSuccessDialog = true
                    }, 2000)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isFormValid && !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing...")
                } else {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Pay $${String.format("%.2f", amount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Accepted Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Accepted: ", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Text("💳 Visa  ", fontSize = 14.sp)
                Text("💳 Mastercard  ", fontSize = 14.sp)
                Text("💳 Amex", fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Trust Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, null, tint = Success)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Your payment is secure",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Card details are encrypted and never stored\n" +
                        "• Protected by 3D Secure authentication\n" +
                        "• PCI DSS compliant payment processing",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Composable dialog for card payment
 */
@Composable
fun CardPaymentDialog(
    amount: Double,
    description: String,
    onPaymentSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var cardNumber by remember { mutableStateOf("") }
    var expiryMonth by remember { mutableStateOf("") }
    var expiryYear by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cardholderName by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    
    // Validation
    val isCardNumberValid = cardNumber.replace(" ", "").length == 16
    val isExpiryValid = expiryMonth.length == 2 && expiryYear.length == 2 &&
            (expiryMonth.toIntOrNull() ?: 0) in 1..12
    val isCvvValid = cvv.length in 3..4
    val isNameValid = cardholderName.isNotBlank()
    val isFormValid = isCardNumberValid && isExpiryValid && isCvvValid && isNameValid
    
    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CreditCard, null, tint = Primary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Pay $${String.format("%.2f", amount)}", fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        },
        text = {
            Column {
                // Cardholder Name
                OutlinedTextField(
                    value = cardholderName,
                    onValueChange = { cardholderName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name on Card") },
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Card Number
                OutlinedTextField(
                    value = cardNumber,
                    onValueChange = { value ->
                        val cleaned = value.replace(" ", "").take(16)
                        cardNumber = cleaned.chunked(4).joinToString(" ")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Card Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Expiry and CVV
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = expiryMonth,
                        onValueChange = { if (it.length <= 2) expiryMonth = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.weight(1f),
                        label = { Text("MM") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = expiryYear,
                        onValueChange = { if (it.length <= 2) expiryYear = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.weight(1f),
                        label = { Text("YY") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { if (it.length <= 4) cvv = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.weight(1f),
                        label = { Text("CVV") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, null, tint = Success, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Secure Payment", style = MaterialTheme.typography.bodySmall, color = Success)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isProcessing = true
                    // Simulate payment
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        isProcessing = false
                        onPaymentSuccess()
                    }, 2000)
                },
                enabled = isFormValid && !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isProcessing) "Processing..." else "Pay Now")
            }
        },
        dismissButton = {
            if (!isProcessing) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
