package com.example.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Category
import com.example.model.FulfillmentMethod
import com.example.model.Product
import com.example.ui.HarvestLinkViewModel
import com.example.ui.common.EmptyStateView
import com.example.ui.common.ProduceImage
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TerracottaAccent

@Composable
fun MarketplaceScreen(
    viewModel: HarvestLinkViewModel,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val products by viewModel.marketplaceProducts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val locationFilter by viewModel.locationFilter.collectAsState()
    val inStockOnly by viewModel.inStockOnly.collectAsState()
    val fulfillmentFilter by viewModel.fulfillmentFilter.collectAsState()

    var showFilterDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search & Filter Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search produce, farm, or location...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = ForestGreenPrimary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ForestGreenPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (locationFilter != null || inStockOnly || fulfillmentFilter != null)
                                TerracottaAccent
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { showFilterDialog = true }
                        .testTag("filter_dialog_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filters",
                        tint = if (locationFilter != null || inStockOnly || fulfillmentFilter != null)
                            Color.White
                        else
                            ForestGreenPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Category Filter Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    val isAllSelected = selectedCategory == null
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { viewModel.setSelectedCategory(null) },
                        label = { Text("All Produce") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("cat_all")
                    )
                }
                items(Category.values()) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setSelectedCategory(if (isSelected) null else category)
                        },
                        label = { Text(category.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ForestGreenPrimary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("cat_${category.name.lowercase()}")
                    )
                }
            }

            // Active filters pills if any
            if (locationFilter != null || inStockOnly || fulfillmentFilter != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Active:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (locationFilter != null) {
                        ActiveFilterPill(
                            label = locationFilter!!,
                            onClear = { viewModel.setLocationFilter(null) }
                        )
                    }
                    if (inStockOnly) {
                        ActiveFilterPill(
                            label = "In Stock",
                            onClear = { viewModel.toggleInStockOnly() }
                        )
                    }
                    if (fulfillmentFilter != null) {
                        ActiveFilterPill(
                            label = fulfillmentFilter!!.label,
                            onClear = { viewModel.setFulfillmentFilter(null) }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = { viewModel.clearAllFilters() },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("Reset All", fontSize = 11.sp, color = TerracottaAccent)
                    }
                }
            }
        }

        // Product Count / Results bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${products.size} listings available direct from farm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Live Farm Prices",
                style = MaterialTheme.typography.labelSmall,
                color = ForestGreenPrimary,
                fontWeight = FontWeight.Bold
            )
        }

        // Products Grid
        if (products.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Storefront,
                title = "No Produce Matches Your Filters",
                subtitle = "Try adjusting your search keywords, price, or location filters.",
                actionButtonText = "Reset All Filters",
                onActionClick = { viewModel.clearAllFilters() },
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 165.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("marketplace_product_grid")
            ) {
                items(products, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product) }
                    )
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        MarketplaceFilterDialog(
            viewModel = viewModel,
            onDismiss = { showFilterDialog = false }
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Photo & Badges Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                ProduceImage(
                    imageUrl = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize()
                )

                // Stock status overlay badge
                if (product.quantityAvailable <= 0.0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFBA1A1A))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "OUT OF STOCK",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (product.quantityAvailable <= 20.0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(TerracottaAccent)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "LOW STOCK",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Fulfillment badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = product.fulfillmentMethod.label,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp
                    )
                }
            }

            // Card Body
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = product.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = "By ${product.farmerName} (${product.farmName})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Price and Available Quantity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "₹${product.pricePerUnit.toInt()}/${product.unit.label}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = ForestGreenPrimary
                        )
                        Text(
                            text = "Min: ${product.minOrderQty.toInt()} ${product.unit.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = "${product.quantityAvailable.toInt()} ${product.unit.label} left",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = if (product.quantityAvailable > 0) ForestGreenPrimary else Color.Red
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveFilterPill(
    label: String,
    onClear: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable { onClear() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(12.dp))
        }
    }
}

@Composable
fun MarketplaceFilterDialog(
    viewModel: HarvestLinkViewModel,
    onDismiss: () -> Unit
) {
    val locationFilter by viewModel.locationFilter.collectAsState()
    val inStockOnly by viewModel.inStockOnly.collectAsState()
    val fulfillmentFilter by viewModel.fulfillmentFilter.collectAsState()

    val regions = listOf("All Regions", "Guntur", "Warangal", "Vijayawada", "Nizamabad", "Khammam")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Filter Produce",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Location filter
                Text(
                    text = "Origin / Farming District",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(regions) { region ->
                        val isSel = if (region == "All Regions") locationFilter == null else locationFilter == region
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                viewModel.setLocationFilter(if (region == "All Regions") null else region)
                            },
                            label = { Text(region, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // In stock toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleInStockOnly() }
                ) {
                    Checkbox(
                        checked = inStockOnly,
                        onCheckedChange = { viewModel.toggleInStockOnly() }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Show In-Stock produce only",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Fulfillment
                Text(
                    text = "Fulfillment Method",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FilterChip(
                        selected = fulfillmentFilter == null,
                        onClick = { viewModel.setFulfillmentFilter(null) },
                        label = { Text("Any", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = fulfillmentFilter == FulfillmentMethod.PICKUP_ONLY,
                        onClick = { viewModel.setFulfillmentFilter(FulfillmentMethod.PICKUP_ONLY) },
                        label = { Text("Pickup", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = fulfillmentFilter == FulfillmentMethod.DELIVERY_ONLY,
                        onClick = { viewModel.setFulfillmentFilter(FulfillmentMethod.DELIVERY_ONLY) },
                        label = { Text("Delivery", fontSize = 12.sp) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
            ) {
                Text("Apply Filters")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    viewModel.clearAllFilters()
                    onDismiss()
                }
            ) {
                Text("Reset All")
            }
        }
    )
}
