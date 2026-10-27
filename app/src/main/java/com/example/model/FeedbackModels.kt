package com.example.model

enum class FeedbackCategory(val label: String) {
    GENERAL("General Experience"),
    APP_BUG("Bug / Glitch Report"),
    PRODUCE_QUALITY("Produce Quality"),
    PAYMENT_DEMO("Demo Payment Feedback"),
    FEATURE_REQUEST("Feature Request")
}

data class Review(
    val id: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val farmerId: String,
    val farmerName: String,
    val buyerId: String,
    val buyerName: String,
    val rating: Int, // 1 to 5 stars
    val comment: String, // max 300 characters
    val verifiedPurchase: Boolean = true,
    val timestampMillis: Long = System.currentTimeMillis(),
    val farmerReply: String? = null,
    val farmerReplyTimestampMillis: Long? = null,
    val hidden: Boolean = false // Admin moderation toggle
)

data class AppFeedback(
    val id: String,
    val userId: String,
    val userName: String,
    val userRole: Role,
    val userContact: String,
    val category: FeedbackCategory,
    val rating: Int? = null,
    val message: String,
    val screenshotNote: String? = null,
    val timestampMillis: Long = System.currentTimeMillis()
)
