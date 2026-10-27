package com.example.ui.admin

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
import com.example.model.AppFeedback
import com.example.model.Product
import com.example.model.Review
import com.example.ui.HarvestLinkViewModel
import com.example.ui.common.ConfirmationDialog
import com.example.ui.common.ProduceImage
import com.example.ui.common.StatCard
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TerracottaAccent

@Composable
fun AdminModerationScreen(
    viewModel: HarvestLinkViewModel,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val allReviews by viewModel.allReviews.collectAsState()
    val appFeedbackList by viewModel.appFeedbackList.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var productToFlag by remember { mutableStateOf<Product?>(null) }
    var flagReasonText by remember { mutableStateOf("Inappropriate pricing or duplicate harvest listing") }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    val duplicatesMap = remember(allProducts) {
        val map = mutableMapOf<String, Boolean>()
        for (p1 in allProducts) {
            val hasDuplicate = allProducts.any { p2 ->
                p1.id != p2.id &&
                p1.farmerId == p2.farmerId &&
                (p1.name.equals(p2.name, ignoreCase = true) ||
                 levenshteinDistance(p1.name.lowercase(), p2.name.lowercase()) <= 3 ||
                 (p1.category == p2.category && p1.name.split(" ").any { word -> word.length > 3 && p2.name.contains(word, ignoreCase = true) }))
            }
            map[p1.id] = hasDuplicate
        }
        map
    }

    val totalCount = allProducts.size
    val activeCount = allProducts.count { it.isActive && !it.flagged }
    val flaggedCount = allProducts.count { it.flagged }

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TerracottaAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = TerracottaAccent, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Admin Moderation Panel",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenPrimary
                        )
                        Text(
                            text = "Marketplace safety, duplicate detection, and content moderation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

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
                        onClick = { selectedTab = 0 },
                        text = { Text("Listings (${allProducts.size})", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("admin_tab_listings")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Reviews (${allReviews.size})", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("admin_tab_reviews")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Feedback (${appFeedbackList.size})", fontWeight = FontWeight.SemiBold) },
                        icon = { Icon(Icons.Default.Feedback, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.testTag("admin_tab_feedback")
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatCard("Total Listings", "$totalCount", "Across all farms", Icons.Default.Inventory, ForestGreenPrimary, Modifier.weight(1f))
                            StatCard("Active Live", "$activeCount", "Visible to buyers", Icons.Default.CheckCircle, Color(0xFF1E6B3A), Modifier.weight(1f))
                            StatCard("Flagged", "$flaggedCount", "Moderated", Icons.Default.Flag, TerracottaAccent, Modifier.weight(1f))
                        }
                    }

                    item {
                        Text("Produce Listings & Moderation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    items(allProducts, key = { it.id }) { product ->
                        val isSuspectedDuplicate = duplicatesMap[product.id] == true
                        AdminProductCard(
                            product = product,
                            isSuspectedDuplicate = isSuspectedDuplicate,
                            onToggleActive = { viewModel.toggleProductActive(product.id) },
                            onFlagClick = {
                                if (product.flagged) {
                                    viewModel.flagProduct(product.id, "")
                                } else {
                                    productToFlag = product
                                }
                            },
                            onDeleteClick = { productToDelete = product }
                        )
                    }
                }
            }

            1 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Customer Reviews Moderation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Admins can hide inappropriate reviews. Hidden reviews are excluded from farmer ratings.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (allReviews.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                                Text("No customer reviews submitted yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(allReviews, key = { it.id }) { review ->
                            AdminReviewCard(
                                review = review,
                                onToggleHide = { viewModel.toggleHideReview(review.id) }
                            )
                        }
                    }
                }
            }

            2 -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("User Feedback & Bug Reports", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Direct feedback sent from user profile screens during the demo hackathon.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (appFeedbackList.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                                Text("No user feedback submitted yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        items(appFeedbackList, key = { it.id }) { feedback ->
                            AdminFeedbackCard(feedback = feedback)
                        }
                    }
                }
            }
        }
    }

    if (productToFlag != null) {
        val prod = productToFlag!!
        AlertDialog(
            onDismissRequest = { productToFlag = null },
            title = { Text("Flag Listing: ${prod.name}") },
            text = {
                Column {
                    Text("Explain the moderation issue for this listing. It will be hidden from the public marketplace:", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = flagReasonText,
                        onValueChange = { flagReasonText = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.flagProduct(prod.id, flagReasonText)
                        productToFlag = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent)
                ) {
                    Text("Flag Listing")
                }
            },
            dismissButton = {
                TextButton(onClick = { productToFlag = null }) { Text("Cancel") }
            }
        )
    }

    if (productToDelete != null) {
        val prod = productToDelete!!
        ConfirmationDialog(
            title = "Delete Produce Listing?",
            message = "Are you sure you want to permanently delete \"${prod.name}\" from ${prod.farmName}? This action cannot be undone.",
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
fun AdminProductCard(
    product: Product,
    isSuspectedDuplicate: Boolean,
    onToggleActive: () -> Unit,
    onFlagClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("admin_item_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (product.flagged)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            else if (!product.isActive)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surface
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
                    Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("Farmer: ${product.farmerName} (${product.farmName}) • ${product.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    Text("₹${product.pricePerUnit.toInt()}/${product.unit.label} • Stock: ${product.quantityAvailable.toInt()} • Min: ${product.minOrderQty.toInt()}", style = MaterialTheme.typography.bodySmall, color = ForestGreenPrimary, fontWeight = FontWeight.Medium)
                }

                if (product.flagged) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFBA1A1A)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("FLAGGED", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                } else if (!product.isActive) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color.Gray.copy(alpha = 0.3f)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("DEACTIVATED", color = Color.DarkGray, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                } else {
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE2EFE7)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("ACTIVE LIVE", color = ForestGreenPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                }
            }

            if (isSuspectedDuplicate) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(Color(0xFFFFF1DC)).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TerracottaAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Duplicate Alert: Similar listing found from same farmer.", style = MaterialTheme.typography.bodySmall, color = TerracottaAccent, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }

            if (product.flagReason != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Flag note: ${product.flagReason}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                OutlinedButton(onClick = onToggleActive, modifier = Modifier.height(34.dp).testTag("admin_toggle_active_${product.id}")) {
                    Icon(if (product.isActive) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (product.isActive) "Deactivate" else "Activate", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onFlagClick, colors = ButtonDefaults.outlinedButtonColors(contentColor = if (product.flagged) ForestGreenPrimary else TerracottaAccent), modifier = Modifier.height(34.dp).testTag("admin_flag_${product.id}")) {
                    Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (product.flagged) "Unflag" else "Flag Listing", fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick, modifier = Modifier.size(34.dp).testTag("admin_delete_${product.id}")) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun AdminReviewCard(
    review: Review,
    onToggleHide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().testTag("admin_review_${review.id}"),
        colors = CardDefaults.cardColors(containerColor = if (review.hidden) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(review.buyerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("For ${review.productName} (Order #${review.orderId})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }

                if (review.hidden) {
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFBA1A1A)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("HIDDEN", color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                } else {
                    Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(Color(0xFFE2EFE7)).padding(horizontal = 8.dp, vertical = 3.dp)) {
                        Text("VISIBLE", color = ForestGreenPrimary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                for (s in 1..5) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = if (s <= review.rating) Color(0xFFD9822B) else Color.LightGray, modifier = Modifier.size(16.dp))
                }
                if (review.verifiedPurchase) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("✓ Verified Purchase", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1E6B3A), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(review.comment, style = MaterialTheme.typography.bodyMedium)

            if (review.farmerReply != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("Farmer reply: \"${review.farmerReply}\"", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onToggleHide,
                    colors = ButtonDefaults.buttonColors(containerColor = if (review.hidden) ForestGreenPrimary else TerracottaAccent),
                    modifier = Modifier.height(34.dp).testTag("admin_toggle_hide_${review.id}")
                ) {
                    Text(if (review.hidden) "Unhide Review" else "Hide Review", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AdminFeedbackCard(
    feedback: AppFeedback,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(feedback.userName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text("${feedback.userRole.name} • ${feedback.userContact}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                }

                Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(ForestGreenPrimary.copy(alpha = 0.12f)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(feedback.category.label, style = MaterialTheme.typography.labelSmall, color = ForestGreenPrimary, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            if (feedback.rating != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (s in 1..5) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = if (s <= feedback.rating) Color(0xFFD9822B) else Color.LightGray, modifier = Modifier.size(14.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(feedback.message, style = MaterialTheme.typography.bodyMedium)

            if (feedback.screenshotNote != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Ref: ${feedback.screenshotNote}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

private fun levenshteinDistance(s1: String, s2: String): Int {
    val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
    for (i in 0..s1.length) dp[i][0] = i
    for (j in 0..s2.length) dp[0][j] = j
    for (i in 1..s1.length) {
        for (j in 1..s2.length) {
            val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
            dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
        }
    }
    return dp[s1.length][s2.length]
}
