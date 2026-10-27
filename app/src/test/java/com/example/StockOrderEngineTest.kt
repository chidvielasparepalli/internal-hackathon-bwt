package com.example

import com.example.logic.PasswordStrength
import com.example.logic.StockOrderEngine
import com.example.model.Category
import com.example.model.FulfillmentChoice
import com.example.model.FulfillmentMethod
import com.example.model.Order
import com.example.model.OrderItem
import com.example.model.OrderStatus
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.Product
import com.example.model.Review
import com.example.model.Role
import com.example.model.UnitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class StockOrderEngineTest {

    private val sampleProduct = Product(
        id = "prod-1",
        farmerId = "farmer-1",
        farmerName = "Ramesh Reddy",
        farmName = "Reddy Organic Farms",
        location = "Guntur, Andhra Pradesh",
        name = "Guntur Red Chillies",
        category = Category.SPICES,
        imageUrl = "https://images.unsplash.com/photo-1588252303782-cb80119abd6d",
        unit = UnitType.KG,
        quantityAvailable = 50.0,
        pricePerUnit = 120.0,
        minOrderQty = 5.0,
        availableFrom = "2026-10-25",
        fulfillmentMethod = FulfillmentMethod.BOTH
    )

    // ORIGINAL 11 TESTS PRESERVED EXACTLY

    @Test
    fun testRejectZeroQuantity() {
        val result = StockOrderEngine.validateQuantity(
            quantity = 0.0,
            availableStock = 50.0,
            minOrderQty = 5.0
        )
        assertTrue(result is StockOrderEngine.QuantityValidationResult.Invalid)
        assertEquals(
            "Quantity must be greater than zero",
            (result as StockOrderEngine.QuantityValidationResult.Invalid).errorMessage
        )
    }

    @Test
    fun testRejectNegativeQuantity() {
        val result = StockOrderEngine.validateQuantity(
            quantity = -10.0,
            availableStock = 50.0,
            minOrderQty = 5.0
        )
        assertTrue(result is StockOrderEngine.QuantityValidationResult.Invalid)
        assertEquals(
            "Quantity must be greater than zero",
            (result as StockOrderEngine.QuantityValidationResult.Invalid).errorMessage
        )
    }

    @Test
    fun testRejectOverStockQuantity() {
        val result = StockOrderEngine.validateQuantity(
            quantity = 60.0,
            availableStock = 50.0,
            minOrderQty = 5.0
        )
        assertTrue(result is StockOrderEngine.QuantityValidationResult.Invalid)
        assertTrue((result as StockOrderEngine.QuantityValidationResult.Invalid).errorMessage.contains("exceeds available stock"))
    }

    @Test
    fun testRespectMinimumOrderQuantity() {
        val result = StockOrderEngine.validateQuantity(
            quantity = 3.0,
            availableStock = 50.0,
            minOrderQty = 5.0
        )
        assertTrue(result is StockOrderEngine.QuantityValidationResult.Invalid)
        assertEquals(
            "Minimum order quantity is 5.0",
            (result as StockOrderEngine.QuantityValidationResult.Invalid).errorMessage
        )
    }

    @Test
    fun testAcceptValidQuantity() {
        val result = StockOrderEngine.validateQuantity(
            quantity = 15.0,
            availableStock = 50.0,
            minOrderQty = 5.0
        )
        assertTrue(result is StockOrderEngine.QuantityValidationResult.Valid)
    }

    @Test
    fun testCorrectTotalCalculation() {
        val item1 = OrderItem(
            productId = "prod-1",
            productName = "Tomatoes",
            quantity = 10.0,
            unit = UnitType.KG,
            unitPriceSnapshot = 30.0
        )
        val item2 = OrderItem(
            productId = "prod-2",
            productName = "Onions",
            quantity = 5.0,
            unit = UnitType.KG,
            unitPriceSnapshot = 40.0
        )
        val total = StockOrderEngine.calculateOrderTotal(listOf(item1, item2))
        assertEquals(500.0, total, 0.001)
    }

    @Test
    fun testStockDecrementOnRequest() {
        val updatedProduct = StockOrderEngine.applyOrderStockReservation(
            product = sampleProduct,
            quantityToReserve = 20.0
        )
        assertEquals(30.0, updatedProduct.quantityAvailable, 0.001)
    }

    @Test
    fun testStockReservationFailsWhenInsufficient() {
        try {
            StockOrderEngine.applyOrderStockReservation(
                product = sampleProduct,
                quantityToReserve = 100.0
            )
            fail("Expected IllegalStateException for insufficient stock")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("Insufficient stock") == true)
        }
    }

    @Test
    fun testStockRestoredOnCancelOrReject() {
        val reservedProduct = StockOrderEngine.applyOrderStockReservation(sampleProduct, 20.0)
        assertEquals(30.0, reservedProduct.quantityAvailable, 0.001)
        val restoredProduct = StockOrderEngine.applyStockRestoration(reservedProduct, 20.0)
        assertEquals(50.0, restoredProduct.quantityAvailable, 0.001)
    }

    @Test
    fun testInvalidStatusTransitionsBlocked() {
        assertFalse(StockOrderEngine.canTransitionOrderStatus(OrderStatus.REQUESTED, OrderStatus.COMPLETED, Role.FARMER))
        assertFalse(StockOrderEngine.canTransitionOrderStatus(OrderStatus.REQUESTED, OrderStatus.READY, Role.FARMER))
        assertFalse(StockOrderEngine.canTransitionOrderStatus(OrderStatus.REQUESTED, OrderStatus.ACCEPTED, Role.BUYER))
    }

    @Test
    fun testCancellationAfterReadyBlocked() {
        assertFalse(StockOrderEngine.canTransitionOrderStatus(OrderStatus.READY, OrderStatus.CANCELLED, Role.BUYER))
        assertFalse(StockOrderEngine.canTransitionOrderStatus(OrderStatus.READY, OrderStatus.CANCELLED, Role.FARMER))
        assertFalse(StockOrderEngine.canTransitionOrderStatus(OrderStatus.READY, OrderStatus.CANCELLED, Role.ADMIN))
        assertFalse(StockOrderEngine.canTransitionOrderStatus(OrderStatus.COMPLETED, OrderStatus.CANCELLED, Role.BUYER))
    }

    @Test
    fun testSimulatedConcurrentOrderCaseOnlyOneSucceeds() {
        val orderRequests = listOf(
            "buyer-alpha" to 35.0,
            "buyer-beta" to 35.0
        )
        val (successfulOrders, finalProduct) = StockOrderEngine.simulateConcurrentOrders(
            initialProduct = sampleProduct,
            orderRequests = orderRequests
        )
        assertEquals(1, successfulOrders.size)
        assertEquals("buyer-alpha", successfulOrders[0].first)
        assertEquals(35.0, successfulOrders[0].second, 0.001)
        assertEquals(15.0, finalProduct.quantityAvailable, 0.001)
    }

    // EXTENDED UNIT TESTS

    @Test
    fun testIndianPhoneAndEmailValidation() {
        assertTrue(StockOrderEngine.isValidIndianPhone("9848012345"))
        assertTrue(StockOrderEngine.isValidIndianPhone("+91 98480 12345"))
        assertTrue(StockOrderEngine.isValidIndianPhone("7893123456"))
        assertFalse(StockOrderEngine.isValidIndianPhone("1234567890")) // Does not start with 6-9
        assertFalse(StockOrderEngine.isValidIndianPhone("984801234")) // 9 digits
        assertFalse(StockOrderEngine.isValidIndianPhone("98480123456")) // 11 digits

        assertTrue(StockOrderEngine.isValidEmail("buyer@harvestlink.org"))
        assertFalse(StockOrderEngine.isValidEmail("plainaddress"))
        assertFalse(StockOrderEngine.isValidEmail("@missingusername.com"))
    }

    @Test
    fun testPasswordStrengthMeter() {
        assertEquals(PasswordStrength.TOO_SHORT, StockOrderEngine.calculatePasswordStrength("short"))
        assertEquals(PasswordStrength.WEAK, StockOrderEngine.calculatePasswordStrength("lowercaseonly"))
        assertTrue(StockOrderEngine.calculatePasswordStrength("Password123") == PasswordStrength.MEDIUM || StockOrderEngine.calculatePasswordStrength("Password123") == PasswordStrength.STRONG)
        assertEquals(PasswordStrength.STRONG, StockOrderEngine.calculatePasswordStrength("DesiFarm@2026"))
    }

    @Test
    fun testPaymentStatusTransitionsAndRefundSimulation() {
        // Cash order defaults to CASH_DUE
        assertEquals(PaymentStatus.CASH_DUE, StockOrderEngine.determinePaymentStatus(PaymentMethod.CASH_ON_PICKUP, true))
        // Online simulated success gives PAID_SIMULATED
        assertEquals(PaymentStatus.PAID_SIMULATED, StockOrderEngine.determinePaymentStatus(PaymentMethod.SIMULATED_UPI, true))
        // Online failure gives FAILED
        assertEquals(PaymentStatus.FAILED, StockOrderEngine.determinePaymentStatus(PaymentMethod.SIMULATED_CARD, false))

        // Refund simulation on cancellation of paid order
        val paidOrder = Order(
            id = "test-1",
            buyerId = "buyer-1",
            buyerName = "Buyer",
            buyerPhone = "9848012345",
            farmerId = "farmer-1",
            farmerName = "Farmer",
            items = emptyList(),
            totalAmount = 200.0,
            fulfillmentChoice = FulfillmentChoice.PICKUP,
            paymentMethod = PaymentMethod.SIMULATED_UPI,
            paymentStatus = PaymentStatus.PAID_SIMULATED,
            status = OrderStatus.ACCEPTED
        )
        val refundStatus = StockOrderEngine.resolveRefundOnCancellation(paidOrder)
        assertEquals(PaymentStatus.REFUND_SIMULATED, refundStatus)
    }

    @Test
    fun testReviewValidationOneReviewPerCompletedOrder() {
        val completedOrder = Order(
            id = "comp-1",
            buyerId = "buyer-1",
            buyerName = "Buyer",
            buyerPhone = "9848012345",
            farmerId = "farmer-1",
            farmerName = "Farmer",
            items = listOf(OrderItem("prod-1", "Chillies", 5.0, UnitType.KG, 120.0)),
            totalAmount = 600.0,
            fulfillmentChoice = FulfillmentChoice.PICKUP,
            status = OrderStatus.COMPLETED
        )

        // Valid review
        val validReviewResult = StockOrderEngine.validateReviewSubmission(
            order = completedOrder,
            rating = 5,
            comment = "Excellent quality fresh harvest!",
            existingReviewsForOrder = emptyList()
        )
        assertTrue(validReviewResult.isValid)

        // Block second review on same completed order
        val existingReview = Review(
            id = "rev-1",
            orderId = "comp-1",
            productId = "prod-1",
            productName = "Chillies",
            farmerId = "farmer-1",
            farmerName = "Farmer",
            buyerId = "buyer-1",
            buyerName = "Buyer",
            rating = 5,
            comment = "First review"
        )
        val duplicateReviewResult = StockOrderEngine.validateReviewSubmission(
            order = completedOrder,
            rating = 4,
            comment = "Second review attempt",
            existingReviewsForOrder = listOf(existingReview)
        )
        assertFalse(duplicateReviewResult.isValid)
        assertTrue(duplicateReviewResult.errorMessage?.contains("already been submitted") == true)
    }

    @Test
    fun testReviewBlockedBeforeCompletion() {
        val inProgressOrder = Order(
            id = "prog-1",
            buyerId = "buyer-1",
            buyerName = "Buyer",
            buyerPhone = "9848012345",
            farmerId = "farmer-1",
            farmerName = "Farmer",
            items = listOf(OrderItem("prod-1", "Chillies", 5.0, UnitType.KG, 120.0)),
            totalAmount = 600.0,
            fulfillmentChoice = FulfillmentChoice.PICKUP,
            status = OrderStatus.ACCEPTED
        )

        val result = StockOrderEngine.validateReviewSubmission(
            order = inProgressOrder,
            rating = 5,
            comment = "Trying to review early",
            existingReviewsForOrder = emptyList()
        )
        assertFalse(result.isValid)
        assertTrue(result.errorMessage?.contains("Completed") == true)
    }

    @Test
    fun testAverageRatingCalculationExcludesHiddenReviews() {
        val rev1 = Review("1", "o1", "p1", "P", "f1", "F", "b1", "B", 5, "Good", hidden = false)
        val rev2 = Review("2", "o2", "p1", "P", "f1", "F", "b2", "B", 3, "Okay", hidden = false)
        val rev3 = Review("3", "o3", "p1", "P", "f1", "F", "b3", "B", 1, "Hidden Spam", hidden = true)

        val avg = StockOrderEngine.calculateAverageRating(listOf(rev1, rev2, rev3))
        // (5 + 3) / 2 = 4.0 (rev3 ignored because hidden = true)
        assertEquals(4.0, avg, 0.001)
    }
}
