package com.example.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.ui.HarvestLinkViewModel
import com.example.ui.common.ConfirmationDialog
import com.example.ui.common.EmptyStateView
import com.example.ui.common.StatusBadge
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TerracottaAccent

@Composable
fun BuyerDashboardScreen(
    viewModel: HarvestLinkViewModel,
    onBrowseMarketplace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()

    val buyerId = currentUser?.id ?: ""
    val myOrders = remember(allOrders, buyerId) {
        allOrders.filter { it.buyerId == buyerId }
    }

    var orderToCancel by remember { mutableStateOf<Order?>(null) }
    var orderToReview by remember { mutableStateOf<Order?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "My Orders & Harvest Status",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
                Text(
                    text = "Every order has a transparent status backed by live transactions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (myOrders.isEmpty()) {
            EmptyStateView(
                title = "No produce orders yet",
                message = "Explore fresh tomatoes, chillies, and orchard fruits directly from Indian farmers.",
                actionButtonText = "Browse Marketplace",
                onActionClick = onBrowseMarketplace,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(myOrders, key = { it.id }) { order ->
                    BuyerOrderCard(
                        order = order,
                        onCancelClick = { orderToCancel = order },
                        onViewReceipt = { viewModel.showOrderReceipt(order) },
                        onWriteReview = { orderToReview = order }
                    )
                }
            }
        }
    }

    if (orderToCancel != null) {
        val o = orderToCancel!!
        ConfirmationDialog(
            title = "Cancel Produce Order #${o.id}?",
            message = "Cancelling will immediately restore ${o.items.firstOrNull()?.quantity?.toInt()} ${o.items.firstOrNull()?.unit?.label} back to the farmer's live stock. " +
                    if (o.paymentStatus == PaymentStatus.PAID_SIMULATED) "Your simulated payment of ₹${o.totalAmount.toInt()} will be refunded to your demo account."
                    else "No cash was collected.",
            confirmButtonText = "Yes, Cancel Order",
            isDestructive = true,
            onConfirm = {
                viewModel.cancelOrder(o.id)
                orderToCancel = null
            },
            onDismiss = { orderToCancel = null }
        )
    }

    if (orderToReview != null) {
        OrderReviewDialog(
            order = orderToReview!!,
            onSubmitReview = { rating, comment ->
                viewModel.submitReview(orderToReview!!.id, rating, comment, onSuccess = {
                    orderToReview = null
                })
            },
            onDismiss = { orderToReview = null }
        )
    }
}

@Composable
fun BuyerOrderCard(
    order: Order,
    onCancelClick: () -> Unit,
    onViewReceipt: () -> Unit,
    onWriteReview: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("buyer_order_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "From: ${order.farmerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // VISUAL ORDER PROGRESS STEPPER
            OrderProgressStepper(status = order.status)

            Spacer(modifier = Modifier.height(10.dp))

            // Items Summary
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity.toInt()} ${item.unit.label} × ${item.productName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "₹${(item.quantity * item.unitPriceSnapshot).toInt()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.paymentMethod.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = order.paymentStatus.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (order.paymentStatus == PaymentStatus.PAID_SIMULATED) Color(0xFF1E6B3A) else if (order.paymentStatus == PaymentStatus.REFUND_SIMULATED) Color(0xFFBA1A1A) else TerracottaAccent
                    )
                }

                Text(
                    text = "Total: ₹${order.totalAmount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = ForestGreenPrimary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            // Actions: Cancel / Receipt / Review
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onViewReceipt,
                    modifier = Modifier.height(36.dp).testTag("buyer_receipt_${order.id}")
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Receipt", fontSize = 11.sp)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Cancellation Rule: allowed while status is "requested" or "accepted". After "ready" not allowed.
                    if (order.status == OrderStatus.REQUESTED || order.status == OrderStatus.ACCEPTED) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.height(36.dp).testTag("cancel_order_button_${order.id}")
                        ) {
                            Text("Cancel Order", fontSize = 11.sp)
                        }
                    } else if (order.status == OrderStatus.READY) {
                        Text(
                            text = "Packed ready (cannot cancel)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    } else if (order.status == OrderStatus.COMPLETED) {
                        if (!order.hasReviewed) {
                            Button(
                                onClick = onWriteReview,
                                colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent),
                                modifier = Modifier.height(36.dp).testTag("write_review_button_${order.id}")
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Write Review", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                text = "✓ Review Submitted",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1E6B3A),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderProgressStepper(status: OrderStatus) {
    if (status == OrderStatus.CANCELLED || status == OrderStatus.REJECTED) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (status == OrderStatus.CANCELLED) "Order Cancelled • Stock Restored to Farm" else "Order Rejected by Farmer • Stock Restored",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
        return
    }

    val steps = listOf(OrderStatus.REQUESTED, OrderStatus.ACCEPTED, OrderStatus.READY, OrderStatus.COMPLETED)
    val currentStepIndex = steps.indexOf(status).coerceAtLeast(0)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isDone = index <= currentStepIndex
            val isCurrent = index == currentStepIndex

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDone) ForestGreenPrimary else Color.LightGray
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    } else {
                        Text("${index + 1}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = step.label,
                    fontSize = 11.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isDone) ForestGreenPrimary else Color.Gray
                )
            }

            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .background(if (index < currentStepIndex) ForestGreenPrimary else Color.LightGray)
                )
            }
        }
    }
}
