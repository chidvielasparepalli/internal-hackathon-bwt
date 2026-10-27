package com.example.logic

import com.example.model.Order
import com.example.model.OrderItemSnapshot
import com.example.model.OrderStatus
import com.example.model.OrderStatusHistoryEntry
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.Product
import com.example.model.Review
import com.example.model.Role

enum class PasswordStrength(val label: String, val score: Int) {
    TOO_SHORT("Too short (<8 chars)", 0),
    WEAK("Weak", 1),
    MEDIUM("Medium", 2),
    STRONG("Strong", 3)
}

data class SignUpValidationResult(
    val isValid: Boolean,
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val locationError: String? = null,
    val roleSpecificError: String? = null
)

data class ReviewValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

object StockOrderEngine {

    sealed class QuantityValidationResult {
        data object Valid : QuantityValidationResult()
        data class Invalid(val errorMessage: String) : QuantityValidationResult() {
            val reason: String get() = errorMessage
        }
    }

    private val INDIAN_PHONE_REGEX = Regex("^[6-9]\\d{9}$")
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    }

    fun isValidIndianPhone(phone: String): Boolean {
        val clean = phone.replace("\\s+".toRegex(), "").replace("-", "").removePrefix("+91")
        return INDIAN_PHONE_REGEX.matches(clean)
    }

    fun calculatePasswordStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.TOO_SHORT
        var score = 0
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isLowerCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        return when {
            score >= 3 && password.length >= 10 -> PasswordStrength.STRONG
            score >= 2 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }

    fun validateSignUp(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        location: String,
        role: Role,
        farmName: String? = null,
        buyerDeliveryAddress: String? = null,
        existingEmails: List<String> = emptyList()
    ): SignUpValidationResult {
        var nameErr: String? = null
        var emailErr: String? = null
        var phoneErr: String? = null
        var passErr: String? = null
        var confirmErr: String? = null
        var locErr: String? = null
        var roleErr: String? = null

        if (fullName.trim().length < 2) {
            nameErr = "Full name must be at least 2 characters"
        }

        val cleanEmail = email.trim().lowercase()
        if (!isValidEmail(cleanEmail)) {
            emailErr = "Please enter a valid email address"
        } else if (existingEmails.any { it.trim().lowercase() == cleanEmail }) {
            emailErr = "Email already registered in system"
        }

        if (!isValidIndianPhone(phone)) {
            phoneErr = "Enter a valid 10-digit Indian phone (e.g. 9848012345)"
        }

        if (password.length < 8) {
            passErr = "Password must be at least 8 characters"
        }

        if (confirmPassword != password) {
            confirmErr = "Passwords do not match"
        }

        if (location.trim().isEmpty()) {
            locErr = "Please specify District and State"
        }

        if (role == Role.FARMER && (farmName == null || farmName.trim().length < 2)) {
            roleErr = "Farm or orchard name is required"
        } else if (role == Role.BUYER && (buyerDeliveryAddress == null || buyerDeliveryAddress.trim().isEmpty())) {
            roleErr = "Delivery address is required"
        }

        val allValid = nameErr == null && emailErr == null && phoneErr == null &&
                passErr == null && confirmErr == null && locErr == null && roleErr == null

        return SignUpValidationResult(
            isValid = allValid,
            nameError = nameErr,
            emailError = emailErr,
            phoneError = phoneErr,
            passwordError = passErr,
            confirmPasswordError = confirmErr,
            locationError = locErr,
            roleSpecificError = roleErr
        )
    }

    fun determinePaymentStatus(paymentMethod: PaymentMethod, simulationSuccess: Boolean): PaymentStatus {
        return when (paymentMethod) {
            PaymentMethod.CASH_ON_PICKUP, PaymentMethod.CASH_ON_DELIVERY -> PaymentStatus.CASH_DUE
            PaymentMethod.SIMULATED_UPI, PaymentMethod.SIMULATED_CARD, PaymentMethod.SIMULATED_NETBANKING -> {
                if (simulationSuccess) PaymentStatus.PAID_SIMULATED else PaymentStatus.FAILED
            }
        }
    }

    fun resolveRefundOnCancellation(order: Order): PaymentStatus {
        return if (order.paymentStatus == PaymentStatus.PAID_SIMULATED) {
            PaymentStatus.REFUND_SIMULATED
        } else {
            order.paymentStatus
        }
    }

    fun validateReviewSubmission(
        order: Order,
        rating: Int,
        comment: String,
        existingReviewsForOrder: List<Review>
    ): ReviewValidationResult {
        if (order.status != OrderStatus.COMPLETED) {
            return ReviewValidationResult(false, "Reviews can only be submitted after the order is Completed")
        }
        if (existingReviewsForOrder.any { it.orderId == order.id }) {
            return ReviewValidationResult(false, "A review has already been submitted for this order")
        }
        if (rating !in 1..5) {
            return ReviewValidationResult(false, "Rating must be between 1 and 5 stars")
        }
        if (comment.trim().isEmpty()) {
            return ReviewValidationResult(false, "Review comment cannot be empty")
        }
        if (comment.length > 300) {
            return ReviewValidationResult(false, "Review comment exceeds 300 characters limit")
        }
        return ReviewValidationResult(true)
    }

    fun calculateAverageRating(reviews: List<Review>): Double {
        val visible = reviews.filter { !it.hidden }
        if (visible.isEmpty()) return 0.0
        val sum = visible.sumOf { it.rating }
        return Math.round((sum.toDouble() / visible.size) * 10.0) / 10.0
    }

    fun validateQuantity(
        quantity: Double,
        availableStock: Double,
        minOrderQty: Double = 1.0
    ): QuantityValidationResult {
        if (quantity.isNaN() || quantity.isInfinite()) {
            return QuantityValidationResult.Invalid("Invalid quantity number")
        }
        if (quantity <= 0.0) {
            return QuantityValidationResult.Invalid("Quantity must be greater than zero")
        }
        if (quantity < minOrderQty) {
            return QuantityValidationResult.Invalid("Minimum order quantity is $minOrderQty")
        }
        if (quantity > availableStock) {
            return QuantityValidationResult.Invalid("Requested quantity ($quantity) exceeds available stock ($availableStock)")
        }
        return QuantityValidationResult.Valid
    }

    fun calculateOrderTotal(items: List<OrderItemSnapshot>): Double {
        return items.sumOf { it.quantity * it.unitPriceSnapshot }
    }

    fun applyOrderStockReservation(
        product: Product,
        quantityToReserve: Double
    ): Product {
        require(quantityToReserve > 0.0) { "Reserved quantity must be strictly positive" }
        if (quantityToReserve > product.quantityAvailable) {
            throw IllegalStateException("Insufficient stock to reserve ($quantityToReserve > ${product.quantityAvailable})")
        }

        val remaining = product.quantityAvailable - quantityToReserve
        return product.copy(
            quantityAvailable = remaining,
            isActive = remaining > 0.0
        )
    }

    fun restoreStockOnCancellation(
        product: Product,
        restoredQuantity: Double
    ): Product {
        require(restoredQuantity > 0.0) { "Restored quantity must be strictly positive" }
        return product.copy(
            quantityAvailable = product.quantityAvailable + restoredQuantity,
            isActive = true
        )
    }

    fun applyStockRestoration(product: Product, quantityToRestore: Double): Product {
        return restoreStockOnCancellation(product, quantityToRestore)
    }

    fun canTransitionOrderStatus(
        currentStatus: OrderStatus,
        targetStatus: OrderStatus,
        actorRole: Role
    ): Boolean {
        if (currentStatus == targetStatus) return false

        return when (currentStatus) {
            OrderStatus.REQUESTED -> {
                when (targetStatus) {
                    OrderStatus.ACCEPTED -> actorRole == Role.FARMER || actorRole == Role.ADMIN
                    OrderStatus.REJECTED -> actorRole == Role.FARMER || actorRole == Role.ADMIN
                    OrderStatus.CANCELLED -> actorRole == Role.BUYER || actorRole == Role.ADMIN
                    else -> false
                }
            }
            OrderStatus.ACCEPTED -> {
                when (targetStatus) {
                    OrderStatus.READY -> actorRole == Role.FARMER || actorRole == Role.ADMIN
                    OrderStatus.CANCELLED -> actorRole == Role.BUYER || actorRole == Role.FARMER || actorRole == Role.ADMIN
                    OrderStatus.REJECTED -> actorRole == Role.FARMER || actorRole == Role.ADMIN
                    else -> false
                }
            }
            OrderStatus.READY -> {
                when (targetStatus) {
                    OrderStatus.COMPLETED -> actorRole == Role.FARMER || actorRole == Role.BUYER || actorRole == Role.ADMIN
                    else -> false
                }
            }
            OrderStatus.COMPLETED -> false
            OrderStatus.CANCELLED -> false
            OrderStatus.REJECTED -> false
        }
    }

    fun transitionOrderStatus(
        order: Order,
        targetStatus: OrderStatus,
        actorRole: Role,
        actorName: String,
        note: String? = null
    ): Order {
        val allowed = canTransitionOrderStatus(order.status, targetStatus, actorRole)
        require(allowed) {
            "Illegal order status transition from ${order.status} to $targetStatus by ${actorRole.name}"
        }

        val historyEntry = OrderStatusHistoryEntry(
            status = targetStatus,
            timestamp = System.currentTimeMillis(),
            actor = "$actorName (${actorRole.name})",
            note = note
        )
        return order.copy(
            status = targetStatus,
            statusHistory = order.statusHistory + historyEntry
        )
    }

    fun simulateConcurrentOrders(
        initialProduct: Product,
        orderRequests: List<Pair<String, Double>>
    ): Pair<List<Pair<String, Double>>, Product> {
        var currentProduct = initialProduct
        val successfulOrders = mutableListOf<Pair<String, Double>>()

        for ((buyerId, requestedQty) in orderRequests) {
            val validation = validateQuantity(requestedQty, currentProduct.quantityAvailable, currentProduct.minOrderQty)
            if (validation is QuantityValidationResult.Valid) {
                currentProduct = applyOrderStockReservation(currentProduct, requestedQty)
                successfulOrders.add(buyerId to requestedQty)
            }
        }
        return successfulOrders to currentProduct
    }
}
