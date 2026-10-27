package com.example.ui.buyer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic.QuantityValidationResult
import com.example.logic.StockOrderEngine
import com.example.model.FulfillmentChoice
import com.example.model.FulfillmentMethod
import com.example.model.Product
import com.example.model.Role
import com.example.ui.HarvestLinkViewModel
import com.example.ui.common.DisclaimerBanner
import com.example.ui.common.ProduceImage
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TerracottaAccent

@Composable
fun ProductDetailScreen(
    product: Product,
    viewModel: HarvestLinkViewModel,
    onBack: () -> Unit,
    onSelectOtherProduct: (Product) -> Unit,
    onPromptSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val allProducts by viewModel.allProducts.collectAsState()
    val farmerProfiles by viewModel.farmerProfiles.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    val isGuest by viewModel.isGuestMode.collectAsState()

    val profile = farmerProfiles.find { it.id == product.farmerId }
    val otherListings = allProducts.filter { it.farmerId == product.farmerId && it.id != product.id && it.isActive && !it.flagged }
    val productReviews = allReviews.filter { (it.productId == product.id || it.farmerId == product.farmerId) && !it.hidden }

    var quantityInput by remember(product.id) { mutableStateOf(product.minOrderQty.toInt().toString()) }
    var fulfillmentChoice by remember(product.id) {
        mutableStateOf(
            if (product.fulfillmentMethod == FulfillmentMethod.PICKUP_ONLY) FulfillmentChoice.PICKUP
            else FulfillmentChoice.DELIVERY
        )
    }
    var deliveryAddress by remember(currentUser) { mutableStateOf(currentUser?.deliveryAddress ?: "") }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    val parsedQty = quantityInput.toDoubleOrNull() ?: 0.0
    val qtyValidation = remember(parsedQty, product.quantityAvailable, product.minOrderQty) {
        StockOrderEngine.validateQuantity(parsedQty, product.quantityAvailable, product.minOrderQty)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth().height(230.dp)) {
                ProduceImage(
                    imageUrl = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(14.dp)
                        .align(Alignment.TopStart)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.5f))
                        .testTag("product_detail_back_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        Text(product.location, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("₹${product.pricePerUnit.toInt()}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = ForestGreenPrimary)
                        Text("per ${product.unit.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(14.dp), tint = ForestGreenPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stock: ${product.quantityAvailable.toInt()} ${product.unit.label}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFD9822B))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("★ ${product.averageRating} (${product.reviewCount})", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Agriculture, contentDescription = null, modifier = Modifier.size(14.dp), tint = TerracottaAccent)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(product.harvestDate, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ORDER PLACEMENT CARD
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("order_placement_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Place Produce Order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        if (isGuest || currentUser == null || currentUser?.role != Role.BUYER) {
                            Surface(color = ForestGreenPrimary.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Sign in with a Buyer account to place orders", fontWeight = FontWeight.Bold, color = ForestGreenPrimary, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = onPromptSignIn,
                                        colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent),
                                        modifier = Modifier.testTag("detail_signin_button")
                                    ) {
                                        Text("Sign in to Order")
                                    }
                                }
                            }
                        } else {
                            // Quantity selector
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Quantity (${product.unit.label})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Min order: ${product.minOrderQty.toInt()} ${product.unit.label}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                }
                                OutlinedTextField(
                                    value = quantityInput,
                                    onValueChange = { quantityInput = it },
                                    modifier = Modifier.width(100.dp).testTag("product_quantity_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                            }

                            if (qtyValidation is QuantityValidationResult.Invalid) {
                                Text(qtyValidation.reason, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                            }

                            // Fulfillment choice
                            Text("Fulfillment Method", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (product.fulfillmentMethod != FulfillmentMethod.DELIVERY_ONLY) {
                                    FilterChip(
                                        selected = fulfillmentChoice == FulfillmentChoice.PICKUP,
                                        onClick = { fulfillmentChoice = FulfillmentChoice.PICKUP },
                                        label = { Text("Farm Pickup") },
                                        modifier = Modifier.weight(1f).testTag("fulfillment_pickup")
                                    )
                                }
                                if (product.fulfillmentMethod != FulfillmentMethod.PICKUP_ONLY) {
                                    FilterChip(
                                        selected = fulfillmentChoice == FulfillmentChoice.DELIVERY,
                                        onClick = { fulfillmentChoice = FulfillmentChoice.DELIVERY },
                                        label = { Text("Delivery (Demo)") },
                                        modifier = Modifier.weight(1f).testTag("fulfillment_delivery")
                                    )
                                }
                            }

                            if (fulfillmentChoice == FulfillmentChoice.DELIVERY) {
                                DisclaimerBanner(
                                    message = "Delivery is represented in this demo only; no logistics network is connected.",
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = deliveryAddress,
                                    onValueChange = { deliveryAddress = it },
                                    label = { Text("Delivery Address") },
                                    modifier = Modifier.fillMaxWidth().testTag("product_delivery_address_input")
                                )
                            }

                            val orderTotal = parsedQty * product.pricePerUnit
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Total Estimate:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("₹${orderTotal.toInt()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = ForestGreenPrimary)
                            }

                            Button(
                                onClick = { showCheckoutDialog = true },
                                enabled = qtyValidation is QuantityValidationResult.Valid && (fulfillmentChoice != FulfillmentChoice.DELIVERY || deliveryAddress.isNotBlank()),
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("product_order_button")
                            ) {
                                Text("Proceed to Checkout", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // FARMER PROFILE CARD
                if (profile != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("About Farmer & Farm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(profile.farmName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Managed by ${profile.farmerName} • ${profile.location}, ${profile.state}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(profile.description, style = MaterialTheme.typography.bodyMedium, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Acreage: ${profile.acreage}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                Text("•", color = Color.Gray)
                                Text("Experience: ${profile.experienceYears} Years", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // REVIEWS SECTION
                Spacer(modifier = Modifier.height(18.dp))
                Text("Verified Customer Reviews (${productReviews.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                if (productReviews.isEmpty()) {
                    Text("No customer reviews for this listing yet. Be the first to order and review!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        productReviews.take(4).forEach { rev ->
                            ReviewItemCard(review = rev)
                        }
                    }
                }

                // OTHER LISTINGS BY THIS FARMER
                if (otherListings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text("More Fresh Harvest from ${product.farmerName}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(otherListings, key = { it.id }) { other ->
                            Card(
                                modifier = Modifier.width(130.dp).clickable { onSelectOtherProduct(other) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(other.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("₹${other.pricePerUnit.toInt()}/${other.unit.label}", style = MaterialTheme.typography.bodySmall, color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showCheckoutDialog) {
        SimulatedCheckoutDialog(
            product = product,
            quantity = parsedQty,
            fulfillmentChoice = fulfillmentChoice,
            deliveryAddress = deliveryAddress,
            onConfirmOrder = { method, simFail ->
                showCheckoutDialog = false
                viewModel.submitOrder(
                    product = product,
                    requestedQty = parsedQty,
                    fulfillmentChoice = fulfillmentChoice,
                    deliveryAddress = deliveryAddress,
                    paymentMethod = method,
                    simulatePaymentFailure = simFail
                )
            },
            onDismiss = { showCheckoutDialog = false }
        )
    }
}
