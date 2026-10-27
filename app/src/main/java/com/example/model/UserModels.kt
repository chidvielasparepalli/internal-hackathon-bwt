package com.example.model

enum class Role {
    BUYER,
    FARMER,
    ADMIN
}

enum class BuyerType(val displayName: String) {
    HOUSEHOLD("Household"),
    RETAILER("Retailer / Kirana"),
    RESTAURANT("Restaurant"),
    WHOLESALER("Wholesaler")
}

data class User(
    val id: String,
    val email: String,
    val name: String,
    val phone: String,
    val location: String,
    val role: Role,
    val buyerType: BuyerType? = null,
    val deliveryAddress: String? = null,
    val profilePhotoUrl: String? = null,
    val passwordHash: String? = null
)

data class FarmerProfile(
    val id: String, // matches farmer's userId
    val farmName: String,
    val farmerName: String,
    val location: String,
    val state: String,
    val description: String,
    val experienceYears: Int = 10,
    val acreage: String = "5 Acres",
    val phone: String = "+91 98480 12345",
    val mainCrops: List<String> = listOf("Vegetables", "Fruits"),
    val profilePhotoUrl: String? = null,
    val averageRating: Double = 4.8,
    val reviewCount: Int = 12
)
