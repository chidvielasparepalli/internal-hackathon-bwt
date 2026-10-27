package com.example.model

enum class OrderStatus(val label: String) {
    REQUESTED("Requested"),
    ACCEPTED("Accepted"),
    READY("Ready"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    REJECTED("Rejected");

    val displayName: String get() = label
}

enum class FulfillmentChoice(val label: String) {
    PICKUP("Farm Pickup"),
    DELIVERY("Home Delivery (Demo)")
}

enum class PaymentMethod(val label: String) {
    CASH_ON_PICKUP("Cash on Pickup"),
    CASH_ON_DELIVERY("Cash on Delivery"),
    SIMULATED_UPI("UPI (Simulated)"),
    SIMULATED_CARD("Debit/Credit Card (Simulated)"),
    SIMULATED_NETBANKING("Net Banking (Simulated)")
}

enum class PaymentStatus(val label: String) {
    PENDING("Payment Pending"),
    CASH_DUE("Cash Due on Fulfillment"),
    PAID_SIMULATED("Paid (Demo Simulation)"),
    FAILED("Payment Failed"),
    REFUND_SIMULATED("Refunded (Demo)")
}

data class OrderItemSnapshot(
    val productId: String,
    val productName: String,
    val quantity: Double,
    val unit: ProduceUnit,
    val unitPriceSnapshot: Double
)

typealias OrderItem = OrderItemSnapshot

data class OrderStatusHistoryEntry(
    val status: OrderStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val actor: String,
    val note: String? = null
)

typealias StatusHistoryItem = OrderStatusHistoryEntry

data class Order(
    val id: String,
    val buyerId: String,
    val buyerName: String,
    val buyerPhone: String,
    val farmerId: String,
    val farmerName: String,
    val items: List<OrderItemSnapshot>,
    val totalAmount: Double,
    val fulfillmentChoice: FulfillmentChoice,
    val deliveryAddress: String? = null,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_PICKUP,
    val paymentStatus: PaymentStatus = PaymentStatus.CASH_DUE,
    val transactionReference: String? = null,
    val paymentTimestampMillis: Long? = null,
    val hasReviewed: Boolean = false,
    val status: OrderStatus = OrderStatus.REQUESTED,
    val statusHistory: List<OrderStatusHistoryEntry> = emptyList(),
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)
