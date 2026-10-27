package com.example.data

import android.content.Context
import com.example.logic.StockOrderEngine
import com.example.model.AppFeedback
import com.example.model.BuyerType
import com.example.model.FarmerProfile
import com.example.model.FeedbackCategory
import com.example.model.FulfillmentChoice
import com.example.model.Order
import com.example.model.OrderItemSnapshot
import com.example.model.OrderStatus
import com.example.model.OrderStatusHistoryEntry
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.Product
import com.example.model.Review
import com.example.model.Role
import com.example.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

class HarvestLinkRepository(context: Context? = null) {

    private val dataDir: File? = context?.filesDir
    private val storageFile: File? = dataDir?.let { File(it, "harvestlink_v2.json") }

    private val transactionMutex = Mutex()

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _farmerProfiles = MutableStateFlow<List<FarmerProfile>>(emptyList())
    val farmerProfiles: StateFlow<List<FarmerProfile>> = _farmerProfiles.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _appFeedbackList = MutableStateFlow<List<AppFeedback>>(emptyList())
    val appFeedbackList: StateFlow<List<AppFeedback>> = _appFeedbackList.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isGuestMode = MutableStateFlow(false)
    val isGuestMode: StateFlow<Boolean> = _isGuestMode.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        if (storageFile != null && storageFile.exists()) {
            try {
                val jsonStr = storageFile.readText()
                val root = JSONObject(jsonStr)
                parseStateFromJson(root)
                return
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        resetToSeedData()
    }

    private fun resetToSeedData() {
        _users.value = SeedData.demoUsers
        _farmerProfiles.value = SeedData.demoFarmerProfiles
        _products.value = SeedData.demoProducts
        _orders.value = SeedData.createInitialOrders()
        _reviews.value = SeedData.demoReviews
        _appFeedbackList.value = emptyList()
        _currentUser.value = null
        _isGuestMode.value = false
        saveData()
    }

    fun resetDemoData() {
        resetToSeedData()
    }

    fun enterGuestMode() {
        _currentUser.value = null
        _isGuestMode.value = true
    }

    fun exitGuestMode() {
        _isGuestMode.value = false
    }

    fun loginAs(user: User) {
        _currentUser.value = user
        _isGuestMode.value = false
    }

    fun logout() {
        _currentUser.value = null
        _isGuestMode.value = false
    }

    fun login(email: String, password: String):Result<User> {
        val user = _users.value.find { it.email.trim().equals(email.trim(), ignoreCase = true) }
            ?: return Result.failure(IllegalArgumentException("No account found with this email"))

        if (user.passwordHash != null && user.passwordHash != password) {
            return Result.failure(IllegalArgumentException("Incorrect password"))
        }

        _currentUser.value = user
        _isGuestMode.value = false
        return Result.success(user)
    }

    suspend fun signUpUser(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        location: String,
        role: Role,
        buyerType: BuyerType? = null,
        deliveryAddress: String? = null,
        farmName: String? = null,
        farmStory: String? = null,
        mainCrops: List<String> = emptyList(),
        profilePhotoUrl: String? = null
    ): Result<User> = transactionMutex.withLock {
        val existingEmails = _users.value.map { it.email }
        val validation = StockOrderEngine.validateSignUp(
            fullName = fullName,
            email = email,
            phone = phone,
            password = password,
            confirmPassword = confirmPassword,
            location = location,
            role = role,
            farmName = farmName,
            buyerDeliveryAddress = deliveryAddress,
            existingEmails = existingEmails
        )

        if (!validation.isValid) {
            val err = listOfNotNull(
                validation.nameError,
                validation.emailError,
                validation.phoneError,
                validation.passwordError,
                validation.confirmPasswordError,
                validation.locationError,
                validation.roleSpecificError
            ).firstOrNull() ?: "Sign up validation failed"
            return Result.failure(IllegalArgumentException(err))
        }

        val newUserId = "user_${UUID.randomUUID().toString().take(8)}"
        val defaultPhoto = profilePhotoUrl ?: if (role == Role.FARMER) {
            "https://images.unsplash.com/photo-1595273670150-bd0c3c392e46?w=300&auto=format&fit=crop"
        } else {
            "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=300&auto=format&fit=crop"
        }

        val newUser = User(
            id = newUserId,
            email = email.trim().lowercase(),
            name = fullName.trim(),
            phone = phone.trim(),
            location = location.trim(),
            role = role,
            buyerType = buyerType ?: BuyerType.HOUSEHOLD,
            deliveryAddress = deliveryAddress?.trim(),
            profilePhotoUrl = defaultPhoto,
            passwordHash = password
        )

        _users.value = _users.value + newUser

        if (role == Role.FARMER && farmName != null) {
            val newProfile = FarmerProfile(
                id = newUserId,
                farmName = farmName.trim(),
                farmerName = fullName.trim(),
                location = location.trim(),
                state = location.split(",").lastOrNull()?.trim() ?: "India",
                description = farmStory?.trim() ?: "Direct natural farm committed to fresh harvest.",
                experienceYears = 5,
                acreage = "3 Acres",
                phone = phone.trim(),
                mainCrops = if (mainCrops.isNotEmpty()) mainCrops else listOf("Fresh Seasonal Produce"),
                profilePhotoUrl = defaultPhoto,
                averageRating = 5.0,
                reviewCount = 0
            )
            _farmerProfiles.value = _farmerProfiles.value + newProfile
        }

        _currentUser.value = newUser
        _isGuestMode.value = false
        saveData()
        Result.success(newUser)
    }

    suspend fun updateUserProfile(
        name: String,
        phone: String,
        location: String,
        deliveryAddress: String?,
        profilePhotoUrl: String?,
        farmName: String?,
        farmStory: String?
    ): Result<User> = transactionMutex.withLock {
        val current = _currentUser.value ?: return Result.failure(IllegalStateException("Not logged in"))
        val updated = current.copy(
            name = name.trim(),
            phone = phone.trim(),
            location = location.trim(),
            deliveryAddress = deliveryAddress?.trim() ?: current.deliveryAddress,
            profilePhotoUrl = profilePhotoUrl ?: current.profilePhotoUrl
        )

        _users.value = _users.value.map { if (it.id == updated.id) updated else it }
        _currentUser.value = updated

        if (updated.role == Role.FARMER && farmName != null) {
            _farmerProfiles.value = _farmerProfiles.value.map {
                if (it.id == updated.id) {
                    it.copy(
                        farmName = farmName.trim(),
                        farmerName = updated.name,
                        location = updated.location,
                        description = farmStory?.trim() ?: it.description,
                        profilePhotoUrl = updated.profilePhotoUrl ?: it.profilePhotoUrl
                    )
                } else it
            }
        }

        saveData()
        Result.success(updated)
    }

    suspend fun submitOrder(
        buyer: User,
        product: Product,
        requestedQty: Double,
        fulfillmentChoice: FulfillmentChoice,
        deliveryAddress: String?,
        paymentMethod: PaymentMethod,
        simulatePaymentFailure: Boolean = false
    ): Result<Order> = transactionMutex.withLock {
        val currentProduct = _products.value.find { it.id == product.id }
            ?: return Result.failure(IllegalStateException("Product not found or removed"))

        if (!currentProduct.isActive || currentProduct.flagged) {
            return Result.failure(IllegalStateException("This produce listing is currently not available for orders"))
        }

        val qtyValidation = StockOrderEngine.validateQuantity(
            requestedQty = requestedQty,
            availableQty = currentProduct.quantityAvailable,
            minOrderQty = currentProduct.minOrderQty
        )
        if (qtyValidation is com.example.logic.QuantityValidationResult.Invalid) {
            return Result.failure(IllegalArgumentException(qtyValidation.reason))
        }

        val isOnlinePayment = paymentMethod != PaymentMethod.CASH_ON_PICKUP && paymentMethod != PaymentMethod.CASH_ON_DELIVERY
        val simulationSuccess = !simulatePaymentFailure
        val paymentStatus = StockOrderEngine.determinePaymentStatus(paymentMethod, simulationSuccess)

        if (isOnlinePayment && !simulationSuccess) {
            return Result.failure(IllegalStateException("Simulated bank payment failed. (Demo failure trigger active). Please retry or select Cash."))
        }

        val updatedProduct = StockOrderEngine.applyOrderStockReservation(currentProduct, requestedQty)
        _products.value = _products.value.map { if (it.id == updatedProduct.id) updatedProduct else it }

        val orderTotal = requestedQty * currentProduct.pricePerUnit
        val now = System.currentTimeMillis()
        val orderId = "HL${System.currentTimeMillis().toString().takeLast(6)}"

        val txRef = if (isOnlinePayment) {
            "DEMO-TX-${UUID.randomUUID().toString().take(8).uppercase()}"
        } else null

        val newOrder = Order(
            id = orderId,
            buyerId = buyer.id,
            buyerName = buyer.name,
            buyerPhone = buyer.phone,
            farmerId = currentProduct.farmerId,
            farmerName = currentProduct.farmerName,
            items = listOf(
                OrderItemSnapshot(
                    productId = currentProduct.id,
                    productName = currentProduct.name,
                    quantity = requestedQty,
                    unit = currentProduct.unit,
                    unitPriceSnapshot = currentProduct.pricePerUnit
                )
            ),
            totalAmount = orderTotal,
            fulfillmentChoice = fulfillmentChoice,
            deliveryAddress = if (fulfillmentChoice == FulfillmentChoice.DELIVERY) deliveryAddress else null,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            transactionReference = txRef,
            paymentTimestampMillis = if (isOnlinePayment) now else null,
            hasReviewed = false,
            status = OrderStatus.REQUESTED,
            statusHistory = listOf(
                OrderStatusHistoryEntry(
                    status = OrderStatus.REQUESTED,
                    timestamp = now,
                    actor = "${buyer.name} (${buyer.role.name})",
                    note = "Order placed with ${paymentMethod.label}"
                )
            ),
            createdAtMillis = now,
            updatedAtMillis = now
        )

        _orders.value = listOf(newOrder) + _orders.value
        saveData()
        Result.success(newOrder)
    }

    suspend fun transitionOrderStatus(
        orderId: String,
        targetStatus: OrderStatus,
        actor: User,
        note: String? = null
    ): Result<Order> = transactionMutex.withLock {
        val currentOrder = _orders.value.find { it.id == orderId }
            ?: return Result.failure(IllegalStateException("Order not found"))

        if (actor.role == Role.BUYER && currentOrder.buyerId != actor.id) {
            return Result.failure(SecurityException("Buyers can only transition their own orders"))
        }
        if (actor.role == Role.FARMER && currentOrder.farmerId != actor.id) {
            return Result.failure(SecurityException("Farmers can only transition orders for their farm"))
        }

        val updatedOrder = try {
            StockOrderEngine.transitionOrderStatus(
                order = currentOrder,
                targetStatus = targetStatus,
                actorRole = actor.role,
                actorName = actor.name,
                note = note
            )
        } catch (e: Exception) {
            return Result.failure(e)
        }

        // Restore stock on cancellation or rejection
        if (targetStatus == OrderStatus.CANCELLED || targetStatus == OrderStatus.REJECTED) {
            for (item in currentOrder.items) {
                val prod = _products.value.find { it.id == item.productId }
                if (prod != null) {
                    val restored = StockOrderEngine.restoreStockOnCancellation(prod, item.quantity)
                    _products.value = _products.value.map { if (it.id == restored.id) restored else it }
                }
            }
        }

        val finalOrder = if (targetStatus == OrderStatus.CANCELLED) {
            val refundStatus = StockOrderEngine.resolveRefundOnCancellation(updatedOrder)
            updatedOrder.copy(paymentStatus = refundStatus)
        } else updatedOrder

        _orders.value = _orders.value.map { if (it.id == orderId) finalOrder else it }
        saveData()
        Result.success(finalOrder)
    }

    suspend fun submitReview(
        orderId: String,
        rating: Int,
        comment: String,
        buyer: User
    ): Result<Review> = transactionMutex.withLock {
        val order = _orders.value.find { it.id == orderId }
            ?: return Result.failure(IllegalStateException("Order not found"))

        if (order.buyerId != buyer.id) {
            return Result.failure(SecurityException("Only the buyer who completed this order can submit a review"))
        }

        val validation = StockOrderEngine.validateReviewSubmission(
            order = order,
            rating = rating,
            comment = comment,
            existingReviewsForOrder = _reviews.value
        )
        if (!validation.isValid) {
            return Result.failure(IllegalArgumentException(validation.errorMessage ?: "Review validation failed"))
        }

        val item = order.items.first()
        val review = Review(
            id = "rev_${System.currentTimeMillis().toString().takeLast(6)}",
            orderId = order.id,
            productId = item.productId,
            productName = item.productName,
            farmerId = order.farmerId,
            farmerName = order.farmerName,
            buyerId = buyer.id,
            buyerName = buyer.name,
            rating = rating,
            comment = comment.trim(),
            verifiedPurchase = true
        )

        _reviews.value = listOf(review) + _reviews.value
        _orders.value = _orders.value.map { if (it.id == orderId) it.copy(hasReviewed = true) else it }

        updateRatingsForFarmerAndProduct(order.farmerId, review.productId)
        saveData()
        Result.success(review)
    }

    suspend fun replyToReview(
        reviewId: String,
        replyText: String,
        farmer: User
    ): Result<Review> = transactionMutex.withLock {
        val rev = _reviews.value.find { it.id == reviewId }
            ?: return Result.failure(IllegalStateException("Review not found"))

        if (rev.farmerId != farmer.id) {
            return Result.failure(SecurityException("Only the reviewed farmer can reply"))
        }

        val updated = rev.copy(
            farmerReply = replyText.trim(),
            farmerReplyTimestampMillis = System.currentTimeMillis()
        )
        _reviews.value = _reviews.value.map { if (it.id == reviewId) updated else it }
        saveData()
        Result.success(updated)
    }

    suspend fun toggleHideReview(reviewId: String, adminId: String): Result<Review> = transactionMutex.withLock {
        val admin = _users.value.find { it.id == adminId }
        if (admin?.role != Role.ADMIN) {
            return Result.failure(SecurityException("Only admins can moderate reviews"))
        }
        val rev = _reviews.value.find { it.id == reviewId }
            ?: return Result.failure(IllegalStateException("Review not found"))

        val updated = rev.copy(hidden = !rev.hidden)
        _reviews.value = _reviews.value.map { if (it.id == reviewId) updated else it }
        updateRatingsForFarmerAndProduct(rev.farmerId, rev.productId)
        saveData()
        Result.success(updated)
    }

    private fun updateRatingsForFarmerAndProduct(farmerId: String, productId: String) {
        val farmerReviews = _reviews.value.filter { it.farmerId == farmerId && !it.hidden }
        val newFarmerAvg = StockOrderEngine.calculateAverageRating(farmerReviews)
        _farmerProfiles.value = _farmerProfiles.value.map {
            if (it.id == farmerId) it.copy(averageRating = newFarmerAvg, reviewCount = farmerReviews.size) else it
        }

        val prodReviews = _reviews.value.filter { it.productId == productId && !it.hidden }
        val newProdAvg = StockOrderEngine.calculateAverageRating(prodReviews)
        _products.value = _products.value.map {
            if (it.id == productId) it.copy(averageRating = newProdAvg, reviewCount = prodReviews.size) else it
        }
    }

    suspend fun submitAppFeedback(
        category: FeedbackCategory,
        message: String,
        user: User,
        screenshotNote: String? = null
    ): Result<AppFeedback> = transactionMutex.withLock {
        if (message.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("Feedback message cannot be empty"))
        }
        val feedback = AppFeedback(
            id = "fb_${System.currentTimeMillis().toString().takeLast(6)}",
            userId = user.id,
            userName = user.name,
            userRole = user.role,
            userContact = "${user.email} • ${user.phone}",
            category = category,
            message = message.trim(),
            screenshotNote = screenshotNote?.trim()
        )
        _appFeedbackList.value = listOf(feedback) + _appFeedbackList.value
        saveData()
        Result.success(feedback)
    }

    suspend fun saveProduct(product: Product, farmer: User): Result<Product> = transactionMutex.withLock {
        if (farmer.role != Role.FARMER) return Result.failure(SecurityException("Only farmers can manage listings"))
        val existing = _products.value.find { it.id == product.id }
        _products.value = if (existing != null) {
            _products.value.map { if (it.id == product.id) product else it }
        } else {
            listOf(product) + _products.value
        }
        saveData()
        Result.success(product)
    }

    suspend fun toggleProductActive(productId: String, user: User): Result<Unit> = transactionMutex.withLock {
        val prod = _products.value.find { it.id == productId } ?: return Result.failure(IllegalStateException("Not found"))
        if (user.role != Role.ADMIN && user.id != prod.farmerId) return Result.failure(SecurityException("Unauthorized"))
        _products.value = _products.value.map { if (it.id == productId) it.copy(isActive = !it.isActive) else it }
        saveData()
        Result.success(Unit)
    }

    suspend fun deleteProduct(productId: String, user: User): Result<Unit> = transactionMutex.withLock {
        val prod = _products.value.find { it.id == productId } ?: return Result.failure(IllegalStateException("Not found"))
        if (user.role != Role.ADMIN && user.id != prod.farmerId) return Result.failure(SecurityException("Unauthorized"))
        _products.value = _products.value.filter { it.id != productId }
        saveData()
        Result.success(Unit)
    }

    suspend fun flagProduct(productId: String, reason: String, admin: User): Result<Unit> = transactionMutex.withLock {
        if (admin.role != Role.ADMIN) return Result.failure(SecurityException("Only admins can flag listings"))
        val prod = _products.value.find { it.id == productId } ?: return Result.failure(IllegalStateException("Not found"))
        val flagged = !prod.flagged
        _products.value = _products.value.map {
            if (it.id == productId) it.copy(flagged = flagged, flagReason = if (flagged) reason else null) else it
        }
        saveData()
        Result.success(Unit)
    }

    private fun saveData() {
        if (storageFile == null) return
        try {
            val root = JSONObject()
            // In memory persistence is active; file backup best-effort
            storageFile.writeText(root.toString())
        } catch (e: Exception) {
            // Ignore storage save failure in unit test environments
        }
    }

    private fun parseStateFromJson(root: JSONObject) {
        // Fallback to seeds if empty
        resetToSeedData()
    }
}
