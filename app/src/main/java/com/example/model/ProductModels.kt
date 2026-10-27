package com.example.model

enum class ProductCategory(val displayName: String) {
    VEGETABLES("Vegetables"),
    FRUITS("Fruits"),
    GRAINS("Grains & Pulses"),
    HERBS("Leafy & Herbs"),
    SPICES("Spices & Condiments")
}

typealias Category = ProductCategory

enum class ProduceUnit(val label: String) {
    KG("kg"),
    QUINTAL("quintal"),
    DOZEN("dozen"),
    BUNCH("bunch"),
    PIECE("piece")
}

typealias UnitType = ProduceUnit

enum class FulfillmentMethod(val label: String) {
    PICKUP_ONLY("Farm Pickup Only"),
    DELIVERY_ONLY("Delivery Only (Demo)"),
    BOTH("Pickup or Delivery")
}

data class Product(
    val id: String,
    val farmerId: String,
    val farmerName: String,
    val farmName: String,
    val name: String,
    val category: ProductCategory,
    val imageUrl: String,
    val unit: ProduceUnit,
    val quantityAvailable: Double,
    val pricePerUnit: Double, // In Indian Rupees (₹)
    val minOrderQty: Double = 1.0,
    val harvestDate: String = "Available Today",
    val availableFrom: String = "2026-10-25",
    val fulfillmentMethod: FulfillmentMethod = FulfillmentMethod.BOTH,
    val location: String = "India",
    val description: String = "",
    val isActive: Boolean = true,
    val flagged: Boolean = false,
    val flagReason: String? = null,
    val averageRating: Double = 4.8,
    val reviewCount: Int = 5
)
