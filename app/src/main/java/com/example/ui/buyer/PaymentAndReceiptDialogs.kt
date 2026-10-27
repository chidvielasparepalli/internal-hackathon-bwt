package com.example.ui.buyer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FulfillmentChoice
import com.example.model.Order
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.Product
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TerracottaAccent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SimulatedCheckoutDialog(
    product: Product,
    quantity: Double,
    fulfillmentChoice: FulfillmentChoice,
    deliveryAddress: String?,
    onConfirmOrder: (PaymentMethod, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val totalAmount = quantity * product.pricePerUnit
    var selectedMethod by remember {
        mutableStateOf(
            if (fulfillmentChoice == FulfillmentChoice.PICKUP) PaymentMethod.CASH_ON_PICKUP
            else PaymentMethod.SIMULATED_UPI
        )
    }
    var simulateFailure by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = ForestGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Checkout & Payment", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = Color(0xFFFFF3CD),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF856404), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Demo payment only; no real money moves. Do NOT enter real card numbers or UPI PINs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF856404),
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${quantity.toInt()} ${product.unit.label} × ${product.name}", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                            Text("₹${totalAmount.toInt()}", fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        }
                        Text("Farmer: ${product.farmerName} • ${fulfillmentChoice.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                }

                Text("Select Payment Method", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                val methods = if (fulfillmentChoice == FulfillmentChoice.PICKUP) {
                    listOf(PaymentMethod.CASH_ON_PICKUP, PaymentMethod.SIMULATED_UPI, PaymentMethod.SIMULATED_CARD, PaymentMethod.SIMULATED_NETBANKING)
                } else {
                    listOf(PaymentMethod.CASH_ON_DELIVERY, PaymentMethod.SIMULATED_UPI, PaymentMethod.SIMULATED_CARD, PaymentMethod.SIMULATED_NETBANKING)
                }

                methods.forEach { method ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).clickable { selectedMethod = method }.padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedMethod == method, onClick = { selectedMethod = method }, modifier = Modifier.testTag("pay_method_${method.name}"))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(method.label, fontSize = 13.sp, fontWeight = if (selectedMethod == method) FontWeight.Bold else FontWeight.Normal)
                    }
                }

                if (selectedMethod != PaymentMethod.CASH_ON_PICKUP && selectedMethod != PaymentMethod.CASH_ON_DELIVERY) {
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Simulate payment failure", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = TerracottaAccent)
                        Switch(checked = simulateFailure, onCheckedChange = { simulateFailure = it }, modifier = Modifier.testTag("simulate_payment_failure_toggle"))
                    }
                }

                AnimatedVisibility(visible = isProcessing) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = ForestGreenPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reserving stock atomically & verifying...", fontSize = 12.sp, color = ForestGreenPrimary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isProcessing = true
                    coroutineScope.launch {
                        delay(500)
                        onConfirmOrder(selectedMethod, simulateFailure)
                    }
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("checkout_confirm_button")
            ) {
                Text(
                    if (selectedMethod == PaymentMethod.CASH_ON_PICKUP || selectedMethod == PaymentMethod.CASH_ON_DELIVERY) "Confirm Order"
                    else "Pay ₹${totalAmount.toInt()} (Simulated)"
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isProcessing) { Text("Cancel") }
        }
    )
}

@Composable
fun OrderReceiptDialog(
    order: Order,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = ForestGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Order Receipt #${order.id}", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    color = ForestGreenPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Status: ${order.status.label.uppercase()}", fontWeight = FontWeight.Bold, color = ForestGreenPrimary, fontSize = 12.sp)
                        Text("Payment: ${order.paymentStatus.label}", fontWeight = FontWeight.Bold, color = if (order.paymentStatus == PaymentStatus.PAID_SIMULATED) Color(0xFF1E6B3A) else TerracottaAccent, fontSize = 12.sp)
                        if (order.transactionReference != null) {
                            Text("Tx Ref: ${order.transactionReference}", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                HorizontalDivider()

                Text("Items Ordered", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                order.items.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.quantity.toInt()} ${item.unit.label} × ${item.productName}", fontSize = 13.sp)
                        Text("₹${(item.quantity * item.unitPriceSnapshot).toInt()}", fontWeight = FontWeight.Bold)
                    }
                    Text("Price snapshot: ₹${item.unitPriceSnapshot.toInt()}/${item.unit.label}", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                HorizontalDivider()

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("₹${order.totalAmount.toInt()}", fontWeight = FontWeight.ExtraBold, color = ForestGreenPrimary, style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text("Farmer: ${order.farmerName} • ${order.fulfillmentChoice.label}", style = MaterialTheme.typography.bodySmall)
                if (order.deliveryAddress != null) {
                    Text("Delivery: ${order.deliveryAddress}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)) {
                Text("Done")
            }
        }
    )
}
