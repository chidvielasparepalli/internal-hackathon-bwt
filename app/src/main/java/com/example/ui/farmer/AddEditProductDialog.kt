package com.example.ui.farmer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Category
import com.example.model.FulfillmentMethod
import com.example.model.Product
import com.example.model.UnitType
import com.example.model.User
import com.example.ui.common.ProduceImage
import com.example.ui.theme.ForestGreenPrimary
import java.util.UUID

@Composable
fun AddEditProductDialog(
    initialProduct: Product?,
    currentUser: User,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var selectedCategory by remember { mutableStateOf(initialProduct?.category ?: Category.VEGETABLES) }
    var imageUrl by remember {
        mutableStateOf(
            initialProduct?.imageUrl ?: "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600&auto=format&fit=crop"
        )
    }
    var selectedUnit by remember { mutableStateOf(initialProduct?.unit ?: UnitType.KG) }
    var quantityText by remember { mutableStateOf(initialProduct?.quantityAvailable?.toInt()?.toString() ?: "50") }
    var priceText by remember { mutableStateOf(initialProduct?.pricePerUnit?.toInt()?.toString() ?: "30") }
    var minOrderText by remember { mutableStateOf(initialProduct?.minOrderQty?.toInt()?.toString() ?: "2") }
    var availableFrom by remember { mutableStateOf(initialProduct?.availableFrom ?: "Today") }
    var fulfillmentMethod by remember { mutableStateOf(initialProduct?.fulfillmentMethod ?: FulfillmentMethod.BOTH) }
    var description by remember { mutableStateOf(initialProduct?.description ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Quick Image Presets for Produce
    val presetImages = listOf(
        "Tomatoes" to "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600&auto=format&fit=crop",
        "Chillies" to "https://images.unsplash.com/photo-1588252303782-cb80119abd6d?w=600&auto=format&fit=crop",
        "Onions" to "https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?w=600&auto=format&fit=crop",
        "Brinjal" to "https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=600&auto=format&fit=crop",
        "Bananas" to "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=600&auto=format&fit=crop",
        "Rice" to "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=600&auto=format&fit=crop"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialProduct == null) "Add New Farm Listing" else "Edit Produce Listing",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Produce Name") },
                        placeholder = { Text("e.g. Country Red Tomatoes") },
                        modifier = Modifier.fillMaxWidth().testTag("product_name_input"),
                        singleLine = true
                    )
                }

                item {
                    Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(Category.values()) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.displayName, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                item {
                    Text("Produce Image", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    // Image Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    ) {
                        ProduceImage(imageUrl = imageUrl, contentDescription = "Preview", modifier = Modifier.fillMaxSize())
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Pick standard photo preset:", style = MaterialTheme.typography.labelSmall)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(presetImages) { (label, url) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (imageUrl == url) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { imageUrl = url }
                            ) {
                                Text(
                                    label,
                                    color = if (imageUrl == url) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Text("Unit of Sale", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(UnitType.values()) { u ->
                            FilterChip(
                                selected = selectedUnit == u,
                                onClick = { selectedUnit = u },
                                label = { Text(u.label, fontSize = 12.sp) }
                            )
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            label = { Text("Price (₹/${selectedUnit.label})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("product_price_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = quantityText,
                            onValueChange = { quantityText = it },
                            label = { Text("Stock (${selectedUnit.label})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("product_stock_input"),
                            singleLine = true
                        )
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = minOrderText,
                            onValueChange = { minOrderText = it },
                            label = { Text("Min Order (${selectedUnit.label})") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = availableFrom,
                            onValueChange = { availableFrom = it },
                            label = { Text("Harvest / Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    Text("Fulfillment Method", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FulfillmentMethod.values().forEach { fMethod ->
                            FilterChip(
                                selected = fulfillmentMethod == fMethod,
                                onClick = { fulfillmentMethod = fMethod },
                                label = { Text(fMethod.label, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Farming Method & Story (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Produce name is required"
                        return@Button
                    }
                    val price = priceText.toDoubleOrNull()
                    if (price == null || price <= 0) {
                        errorMessage = "Please enter a valid price"
                        return@Button
                    }
                    val qty = quantityText.toDoubleOrNull()
                    if (qty == null || qty < 0) {
                        errorMessage = "Please enter a valid stock quantity"
                        return@Button
                    }
                    val minOrder = minOrderText.toDoubleOrNull() ?: 1.0

                    val productToSave = Product(
                        id = initialProduct?.id ?: ("prod-" + UUID.randomUUID().toString().take(8)),
                        farmerId = initialProduct?.farmerId ?: currentUser.id,
                        farmerName = initialProduct?.farmerName ?: currentUser.name,
                        farmName = initialProduct?.farmName ?: "${currentUser.name}'s Natural Farm",
                        location = initialProduct?.location ?: currentUser.location,
                        name = name.trim(),
                        category = selectedCategory,
                        imageUrl = imageUrl.trim(),
                        unit = selectedUnit,
                        quantityAvailable = qty,
                        pricePerUnit = price,
                        minOrderQty = minOrder,
                        availableFrom = availableFrom.trim().ifBlank { "Fresh Daily" },
                        fulfillmentMethod = fulfillmentMethod,
                        isActive = initialProduct?.isActive ?: true,
                        flagged = initialProduct?.flagged ?: false,
                        description = description.trim()
                    )
                    onSave(productToSave)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("save_product_button")
            ) {
                Text(if (initialProduct == null) "Add Listing" else "Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
