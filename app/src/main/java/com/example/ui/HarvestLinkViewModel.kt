package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HarvestLinkRepository
import com.example.model.AppFeedback
import com.example.model.BuyerType
import com.example.model.FarmerProfile
import com.example.model.FeedbackCategory
import com.example.model.FulfillmentChoice
import com.example.model.FulfillmentMethod
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.Review
import com.example.model.Role
import com.example.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FarmerStats(
    val totalSalesCompleted: Double,
    val pendingOrdersCount: Int,
    val lowStockCount: Int,
    val totalCashDue: Double,
    val totalPaidSimulated: Double
)

data class BuyerFilter(
    val query: String = "",
    val category: ProductCategory? = null,
    val location: String = "",
    val maxPrice: Double = 1000.0,
    val inStockOnly: Boolean = false,
    val fulfillment: FulfillmentMethod? = null
)

class HarvestLinkViewModel(
    application: Application,
    val repository: HarvestLinkRepository = HarvestLinkRepository(application)
) : AndroidViewModel(application) {

    val allProducts: StateFlow<List<Product>> = repository.products
    val allOrders: StateFlow<List<Order>> = repository.orders
    val allReviews: StateFlow<List<Review>> = repository.reviews
    val appFeedbackList: StateFlow<List<AppFeedback>> = repository.appFeedbackList
    val allUsers: StateFlow<List<User>> = repository.users
    val farmerProfiles: StateFlow<List<FarmerProfile>> = repository.farmerProfiles
    val currentUser: StateFlow<User?> = repository.currentUser
    val isGuestMode: StateFlow<Boolean> = repository.isGuestMode

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _selectedReceiptOrder = MutableStateFlow<Order?>(null)
    val selectedReceiptOrder: StateFlow<Order?> = _selectedReceiptOrder.asStateFlow()

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _buyerFilter = MutableStateFlow(BuyerFilter())
    val buyerFilter: StateFlow<BuyerFilter> = _buyerFilter.asStateFlow()

    val filteredProducts: StateFlow<List<Product>> = combine(
        allProducts,
        _buyerFilter
    ) { products, filter ->
        products.filter { product ->
            val matchesQuery = filter.query.isBlank() ||
                    product.name.contains(filter.query, ignoreCase = true) ||
                    product.farmerName.contains(filter.query, ignoreCase = true) ||
                    product.location.contains(filter.query, ignoreCase = true)

            val matchesCategory = filter.category == null || product.category == filter.category
            val matchesLocation = filter.location.isBlank() || product.location.contains(filter.location, ignoreCase = true)
            val matchesPrice = product.pricePerUnit <= filter.maxPrice
            val matchesStock = !filter.inStockOnly || product.quantityAvailable > 0.0
            val matchesFulfillment = filter.fulfillment == null ||
                    product.fulfillmentMethod == filter.fulfillment ||
                    product.fulfillmentMethod == FulfillmentMethod.BOTH

            matchesQuery && matchesCategory && matchesLocation && matchesPrice && matchesStock && matchesFulfillment && product.isActive && !product.flagged
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _buyerFilter.value = _buyerFilter.value.copy(query = query)
    }

    fun setSelectedCategory(category: ProductCategory?) {
        _buyerFilter.value = _buyerFilter.value.copy(category = category)
    }

    fun setSelectedLocation(location: String) {
        _buyerFilter.value = _buyerFilter.value.copy(location = location)
    }

    fun setMaxPrice(price: Double) {
        _buyerFilter.value = _buyerFilter.value.copy(maxPrice = price)
    }

    fun setInStockOnly(inStock: Boolean) {
        _buyerFilter.value = _buyerFilter.value.copy(inStockOnly = inStock)
    }

    fun setSelectedFulfillment(method: FulfillmentMethod?) {
        _buyerFilter.value = _buyerFilter.value.copy(fulfillment = method)
    }

    fun resetFilters() {
        _buyerFilter.value = BuyerFilter()
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun showOrderReceipt(order: Order?) {
        _selectedReceiptOrder.value = order
    }

    fun enterGuestMode() {
        repository.enterGuestMode()
    }

    fun exitGuestMode() {
        repository.exitGuestMode()
    }

    fun loginAs(user: User) {
        repository.loginAs(user)
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.login(email, pass)
            if (result.isSuccess) {
                _userMessage.emit("Welcome back, ${result.getOrNull()?.name}!")
                onSuccess()
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun logout() {
        repository.logout()
    }

    fun resetDemoData() {
        repository.resetDemoData()
        viewModelScope.launch {
            _userMessage.emit("Demo data restored to initial hackathon showcase state")
        }
    }

    fun signUpUser(
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
        profilePhotoUrl: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.signUpUser(
                fullName = fullName,
                email = email,
                phone = phone,
                password = password,
                confirmPassword = confirmPassword,
                location = location,
                role = role,
                buyerType = buyerType,
                deliveryAddress = deliveryAddress,
                farmName = farmName,
                farmStory = farmStory,
                mainCrops = mainCrops,
                profilePhotoUrl = profilePhotoUrl
            )
            if (result.isSuccess) {
                _userMessage.emit("Account created successfully! Welcome to HarvestLink.")
                onSuccess()
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Sign up failed")
            }
        }
    }

    fun updateUserProfile(
        name: String,
        phone: String,
        location: String,
        deliveryAddress: String?,
        profilePhotoUrl: String?,
        farmName: String?,
        farmStory: String?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.updateUserProfile(name, phone, location, deliveryAddress, profilePhotoUrl, farmName, farmStory)
            if (result.isSuccess) {
                _userMessage.emit("Profile updated successfully")
                onSuccess()
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Failed to update profile")
            }
        }
    }

    fun submitOrder(
        product: Product,
        requestedQty: Double,
        fulfillmentChoice: FulfillmentChoice,
        deliveryAddress: String?,
        paymentMethod: PaymentMethod,
        simulatePaymentFailure: Boolean = false,
        onSuccess: (Order) -> Unit = {}
    ) {
        val user = currentUser.value
        if (user == null || user.role != Role.BUYER) {
            viewModelScope.launch { _userMessage.emit("Please sign in with a Buyer account to place orders") }
            return
        }

        viewModelScope.launch {
            val result = repository.submitOrder(
                buyer = user,
                product = product,
                requestedQty = requestedQty,
                fulfillmentChoice = fulfillmentChoice,
                deliveryAddress = deliveryAddress,
                paymentMethod = paymentMethod,
                simulatePaymentFailure = simulatePaymentFailure
            )
            if (result.isSuccess) {
                val order = result.getOrNull()!!
                _userMessage.emit("Order #${order.id} submitted! Stock reserved atomically.")
                _selectedReceiptOrder.value = order
                _selectedProduct.value = null
                onSuccess(order)
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Failed to place order")
            }
        }
    }

    fun transitionOrderStatus(orderId: String, targetStatus: OrderStatus, note: String? = null) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.transitionOrderStatus(orderId, targetStatus, user, note)
            if (result.isSuccess) {
                _userMessage.emit("Order #${orderId} moved to ${targetStatus.label}")
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Failed to update order status")
            }
        }
    }

    fun cancelOrder(orderId: String) {
        transitionOrderStatus(orderId, OrderStatus.CANCELLED, "Cancelled by buyer. Stock restored.")
    }

    fun submitReview(orderId: String, rating: Int, comment: String, onSuccess: () -> Unit = {}) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.submitReview(orderId, rating, comment, user)
            if (result.isSuccess) {
                _userMessage.emit("Review submitted! Thank you for supporting our farmers.")
                onSuccess()
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Failed to submit review")
            }
        }
    }

    fun replyToReview(reviewId: String, replyText: String, onSuccess: () -> Unit = {}) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.replyToReview(reviewId, replyText, user)
            if (result.isSuccess) {
                _userMessage.emit("Reply posted to review")
                onSuccess()
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Failed to reply")
            }
        }
    }

    fun toggleHideReview(reviewId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.toggleHideReview(reviewId, user.id)
            if (result.isSuccess) {
                _userMessage.emit("Review visibility updated")
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Failed to update review visibility")
            }
        }
    }

    fun submitAppFeedback(
        category: FeedbackCategory,
        message: String,
        rating: Int? = null,
        screenshotNote: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.submitAppFeedback(category, message, user, screenshotNote)
            if (result.isSuccess) {
                _userMessage.emit("Feedback submitted! Thank you.")
                onSuccess()
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Failed to send feedback")
            }
        }
    }

    fun saveProduct(product: Product) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.saveProduct(product, user)
            if (result.isSuccess) {
                _userMessage.emit("Produce listing saved successfully")
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Failed to save listing")
            }
        }
    }

    fun toggleProductActive(productId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.toggleProductActive(productId, user)
            if (result.isSuccess) {
                _userMessage.emit("Listing status updated")
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Failed to update listing")
            }
        }
    }

    fun deleteProduct(productId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.deleteProduct(productId, user)
            if (result.isSuccess) {
                _userMessage.emit("Listing deleted")
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Failed to delete listing")
            }
        }
    }

    fun flagProduct(productId: String, reason: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val result = repository.flagProduct(productId, reason, user)
            if (result.isSuccess) {
                _userMessage.emit("Listing moderation flag updated")
            } else {
                _userMessage.emit(result.exceptionOrNull()?.message ?: "Moderation action failed")
            }
        }
    }

    fun getFarmerStats(farmerId: String, products: List<Product>, orders: List<Order>): FarmerStats {
        val farmerOrders = orders.filter { it.farmerId == farmerId }
        val farmerProducts = products.filter { it.farmerId == farmerId }

        val completed = farmerOrders.filter { it.status == OrderStatus.COMPLETED }
        val totalSales = completed.sumOf { it.totalAmount }
        val pending = farmerOrders.count { it.status == OrderStatus.REQUESTED || it.status == OrderStatus.ACCEPTED }
        val lowStock = farmerProducts.count { it.quantityAvailable in 0.1..20.0 }

        val cashDue = farmerOrders.filter { it.paymentStatus == PaymentStatus.CASH_DUE && it.status != OrderStatus.CANCELLED && it.status != OrderStatus.REJECTED }.sumOf { it.totalAmount }
        val paidSimulated = farmerOrders.filter { it.paymentStatus == PaymentStatus.PAID_SIMULATED }.sumOf { it.totalAmount }

        return FarmerStats(
            totalSalesCompleted = totalSales,
            pendingOrdersCount = pending,
            lowStockCount = lowStock,
            totalCashDue = cashDue,
            totalPaidSimulated = paidSimulated
        )
    }
}
