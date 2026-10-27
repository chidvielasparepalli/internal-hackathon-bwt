package com.example.ui.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.model.Product
import com.example.ui.HarvestLinkViewModel
import com.example.ui.buyer.ReviewItemCard
import com.example.ui.common.ConfirmationDialog
import com.example.ui.common.EmptyStateView
import com.example.ui.common.ProduceImage
import com.example.ui.common.StatCard
import com.example.ui.common.StatusBadge
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TerracottaAccent

@Composable
fun FarmerWorkspaceScreen(
    viewModel: HarvestLinkViewModel,
    activeTab: Int = 0,
    onTabChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()

    val farmerId = currentUser?.id ?: ""
    val farmerProducts = remember(allProducts, farmerId) { allProducts.filter { it.farmerId == farmerId } }
    val farmerOrders = remember(allOrders, farmerId) { allOrders.filter { it.farmerId == farmerId } }
    val farmerReviews = remember(allReviews, farmerId) { allReviews.filter { it.farmerId == farmerId && !it.hidden } }

    val stats = remember(farmerProducts, farmerOrders, farmerId) {
        viewModel.getFarmerStats(farmerId, allProducts, allOrders)
    }

    var selectedTab by remember { mutableIntStateOf(activeTab.coerceIn(0, 2)) }
    LaunchedEffect(activeTab) {
        if (activeTab in 0..2) selectedTab = activeTab
    }

    var showAddEditDialog by remember { mutableStateOf(false) }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    Scaffold(
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        productToEdit = null
                        showAddEditDialog = true
                    },
                    containerColor = ForestGreenPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("farmer_add_produce_fab")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Produce")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Produce", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Workspace Header
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text("Farmer Workspace", style = MaterialTheme.typography.headlineMedium, color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                    Text("${currentUser?.name} • ${currentUser?.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = ForestGreenPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = ForestGreenPrimary)
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0; onTabChange(0) },
                            text = { Text("Inventory (${farmerProducts.size})", fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("tab_inventory")
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1; onTabChange(1) },
                            text = {
                                val pending = stats.pendingOrdersCount
                                Text(
                                    if (pending > 0) "Orders ($pending Pending)" else "Orders (${farmerOrders.size})",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (pending > 0 && selectedTab != 1) TerracottaAccent else ForestGreenPrimary
                                )
                            },
                            modifier = Modifier.testTag("tab_orders")
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2; onTabChange(2) },
                            text = { Text("Reviews (${farmerReviews.size})", fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("tab_reviews")
                        )
                    }
                }
            }

            // Tab 0: Inventory & Overview
            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                StatCard("Completed Sales", "₹${stats.totalSalesCompleted.toInt()}", "Total income", Icons.Default.CurrencyRupee, ForestGreenPrimary, Modifier.weight(1f))
                                StatCard("Pending Orders", "${stats.pendingOrdersCount}", "Needs action", Icons.Default.PendingActions, if (stats.pendingOrdersCount > 0) TerracottaAccent else ForestGreenPrimary, Modifier.weight(1f))
                                StatCard("Low Stock", "${stats.lowStockCount}", "≤20 units", Icons.Default.Warning, Color(0xFFD9822B), Modifier.weight(1f))
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceAround) {
                                    Text("Cash to Collect: ₹${stats.totalCashDue.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFFB57C2A))
                                    Text("•", color = Color.Gray)
                                    Text("Simulated Online Paid: ₹${stats.totalPaidSimulated.toInt()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color(0xFF1E6B3A))
                                }
                            }
                        }
                    }

                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Produce Listings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Button(
                                onClick = {
                                    productToEdit = null
                                    showAddEditDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                modifier = Modifier.height(34.dp).testTag("farmer_add_produce_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Harvest", fontSize = 11.sp)
                            }
                        }
                    }

                    if (farmerProducts.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No produce listed yet",
                                message = "Add your fresh farm vegetables, orchard fruits, or grains to start selling directly.",
                                actionButtonText = "Add Your First Produce",
                                onActionClick = {
                                    productToEdit = null
                                    showAddEditDialog = true
                                }
                            )
                        }
                    } else {
                        items(farmerProducts, key = { it.id }) { product ->
                            FarmerProductCard(
                                product = product,
                                onEdit = {
                                    productToEdit = product
                                    showAddEditDialog = true
                                },
                                onToggleActive = { viewModel.toggleProductActive(product.id) },
                                onDelete = { productToDelete = product }
                            )
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // Tab 1: Incoming Orders
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Incoming Customer Orders", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Accept, prepare, and complete orders with atomic stock synchronization.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (farmerOrders.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No incoming orders yet",
                                message = "When buyers place orders for your produce, they will appear here with live status updates."
                            )
                        }
                    } else {
                        items(farmerOrders, key = { it.id }) { order ->
                            FarmerOrderCard(
                                order = order,
                                onAccept = { viewModel.transitionOrderStatus(order.id, OrderStatus.ACCEPTED, "Accepted by farmer") },
                                onReject = { viewModel.transitionOrderStatus(order.id, OrderStatus.REJECTED, "Rejected by farmer. Stock restored.") },
                                onMarkReady = { viewModel.transitionOrderStatus(order.id, OrderStatus.READY, "Packed and ready for pickup/delivery") },
                                onMarkCompleted = { viewModel.transitionOrderStatus(order.id, OrderStatus.COMPLETED, "Completed and fulfilled") },
                                onViewReceipt = { viewModel.showOrderReceipt(order) }
                            )
                        }
                    }
                }
            } else {
                // Tab 2: Reviews
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Customer Reviews & Ratings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Verified buyer reviews from completed orders. Farmers can reply to customer reviews.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (farmerReviews.isEmpty()) {
                        item {
                            EmptyStateView(
                                title = "No customer reviews yet",
                                message = "Customer reviews will appear here once buyers complete their orders."
                            )
                        }
                    } else {
                        items(farmerReviews, key = { it.id }) { review ->
                            ReviewItemCard(
                                review = review,
                                isFarmerOwner = true,
                                onReplySubmit = { reply ->
                                    viewModel.replyToReview(review.id, reply)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditProductDialog(
            farmerUser = currentUser!!,
            existingProduct = productToEdit,
            onSave = { updated ->
                viewModel.saveProduct(updated)
                showAddEditDialog = false
            },
            onDismiss = { showAddEditDialog = false }
        )
    }

    if (productToDelete != null) {
        val prod = productToDelete!!
        ConfirmationDialog(
            title = "Delete '${prod.name}'?",
            message = "Are you sure you want to remove this listing? This will remove it from the public buyer marketplace.",
            confirmButtonText = "Delete Listing",
            isDestructive = true,
            onConfirm = {
                viewModel.deleteProduct(prod.id)
                productToDelete = null
            },
            onDismiss = { productToDelete = null }
        )
    }
}

@Composable
fun FarmerProductCard(
    product: Product,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("farmer_produce_item_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (!product.isActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp))) {
                    ProduceImage(imageUrl = product.imageUrl, contentDescription = product.name, modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(product.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("₹${product.pricePerUnit.toInt()}/${product.unit.label} • Stock: ${product.quantityAvailable.toInt()} • Min: ${product.minOrderQty.toInt()}", style = MaterialTheme.typography.bodySmall, color = ForestGreenPrimary, fontWeight = FontWeight.Medium)
                    Text("★ ${product.averageRating} (${product.reviewCount} reviews) • ${product.fulfillmentMethod.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }

                if (!product.isActive) {
                    Surface(color = Color.LightGray.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp)) {
                        Text("PAUSED", color = Color.DarkGray, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                } else if (product.quantityAvailable <= 20.0) {
                    Surface(color = Color(0xFFFFF1DC), shape = RoundedCornerShape(4.dp)) {
                        Text("LOW STOCK", color = TerracottaAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onToggleActive, modifier = Modifier.height(34.dp).testTag("farmer_toggle_active_${product.id}")) {
                    Text(if (product.isActive) "Pause Listing" else "Activate Listing", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Button(onClick = onEdit, colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary), modifier = Modifier.height(34.dp).testTag("farmer_edit_${product.id}")) {
                    Text("Edit", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp).testTag("farmer_delete_${product.id}")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun FarmerOrderCard(
    order: Order,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onMarkReady: () -> Unit,
    onMarkCompleted: () -> Unit,
    onViewReceipt: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("farmer_order_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Order #${order.id}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Buyer: ${order.buyerName} (+91 ${order.buyerPhone})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(status = order.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            order.items.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.quantity.toInt()} ${item.unit.label} × ${item.productName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text("₹${(item.quantity * item.unitPriceSnapshot).toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(order.paymentStatus.label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                Text("Total: ₹${order.totalAmount.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = ForestGreenPrimary)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onViewReceipt, modifier = Modifier.height(36.dp)) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Receipt", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))

                when (order.status) {
                    OrderStatus.REQUESTED -> {
                        OutlinedButton(onClick = onReject, colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), modifier = Modifier.height(36.dp)) {
                            Text("Reject", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(onClick = onAccept, colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary), modifier = Modifier.height(36.dp)) {
                            Text("Accept Request", fontSize = 11.sp)
                        }
                    }
                    OrderStatus.ACCEPTED -> {
                        Button(onClick = onMarkReady, colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary), modifier = Modifier.height(36.dp)) {
                            Text("Mark Ready", fontSize = 11.sp)
                        }
                    }
                    OrderStatus.READY -> {
                        Button(onClick = onMarkCompleted, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E6B3A)), modifier = Modifier.height(36.dp)) {
                            Text("Mark Completed", fontSize = 11.sp)
                        }
                    }
                    OrderStatus.COMPLETED -> {
                        Text("Completed", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1E6B3A), fontWeight = FontWeight.Bold)
                    }
                    OrderStatus.CANCELLED -> {
                        Text("Cancelled (Stock Restored)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                    OrderStatus.REJECTED -> {
                        Text("Rejected (Stock Restored)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
