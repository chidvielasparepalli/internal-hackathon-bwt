package com.example.data

import com.example.model.BuyerType
import com.example.model.FarmerProfile
import com.example.model.FulfillmentChoice
import com.example.model.FulfillmentMethod
import com.example.model.Order
import com.example.model.OrderItemSnapshot
import com.example.model.OrderStatus
import com.example.model.OrderStatusHistoryEntry
import com.example.model.PaymentMethod
import com.example.model.PaymentStatus
import com.example.model.Product
import com.example.model.ProductCategory
import com.example.model.ProduceUnit
import com.example.model.Review
import com.example.model.Role
import com.example.model.User

object SeedData {

    val demoUsers = listOf(
        User(
            id = "buyer_priya",
            email = "priya.nambiar@example.com",
            name = "Priya Nambiar",
            phone = "9848011223",
            location = "Hyderabad, Telangana",
            role = Role.BUYER,
            buyerType = BuyerType.HOUSEHOLD,
            deliveryAddress = "Flat 402, Green Meadows, Banjara Hills Road 12, Hyderabad",
            profilePhotoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=300&auto=format&fit=crop",
            passwordHash = "DemoPass123"
        ),
        User(
            id = "buyer_swagath",
            email = "procurement@swagathgrand.com",
            name = "Swagath Grand Hotel (Kiran)",
            phone = "9849022334",
            location = "Visakhapatnam, Andhra Pradesh",
            role = Role.BUYER,
            buyerType = BuyerType.RESTAURANT,
            deliveryAddress = "Swagath Grand, Beach Road, Siripuram, Visakhapatnam",
            profilePhotoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300&auto=format&fit=crop",
            passwordHash = "DemoPass123"
        ),
        User(
            id = "farmer_ramesh",
            email = "ramesh.reddy@harvestlink.org",
            name = "Ramesh Reddy",
            phone = "9848033445",
            location = "Guntur, Andhra Pradesh",
            role = Role.FARMER,
            profilePhotoUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=300&auto=format&fit=crop",
            passwordHash = "DemoPass123"
        ),
        User(
            id = "farmer_anitha",
            email = "anitha.rao@harvestlink.org",
            name = "Anitha Rao",
            phone = "9848044556",
            location = "Warangal, Telangana",
            role = Role.FARMER,
            profilePhotoUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=300&auto=format&fit=crop",
            passwordHash = "DemoPass123"
        ),
        User(
            id = "admin_hackathon",
            email = "admin@harvestlink.org",
            name = "Suresh Verma (Moderator)",
            phone = "9848099887",
            location = "Hyderabad, Telangana",
            role = Role.ADMIN,
            profilePhotoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300&auto=format&fit=crop",
            passwordHash = "AdminSecure2026"
        )
    )

    val demoFarmerProfiles = listOf(
        FarmerProfile(
            id = "farmer_ramesh",
            farmName = "Reddy Organic & Natural Farms",
            farmerName = "Ramesh Reddy",
            location = "Tenali Road, Guntur",
            state = "Andhra Pradesh",
            description = "Family-run natural farm specializing in indigenous Krishna valley chillies, cluster beans, and sun-ripened country tomatoes grown without chemical pesticides.",
            experienceYears = 18,
            acreage = "12 Acres",
            phone = "+91 98480 33445",
            mainCrops = listOf("Guntur Chillies", "Country Tomatoes", "Banganapalli Mangoes", "Desi Okra"),
            profilePhotoUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=400&auto=format&fit=crop",
            averageRating = 4.9,
            reviewCount = 14
        ),
        FarmerProfile(
            id = "farmer_anitha",
            farmName = "Kakatiya Heritage Orchards",
            farmerName = "Anitha Rao",
            location = "Narsampet Rural, Warangal",
            state = "Telangana",
            description = "Pioneering women-led organic cooperative. We grow heritage red onions, ginger, and robusta banana varieties irrigated with harvested rainwater and vermicompost.",
            experienceYears = 11,
            acreage = "8.5 Acres",
            phone = "+91 98480 44556",
            mainCrops = listOf("Heritage Red Onions", "Fresh Turmeric", "Robusta Banana", "Sweet Lime"),
            profilePhotoUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?w=400&auto=format&fit=crop",
            averageRating = 4.8,
            reviewCount = 9
        )
    )

    val demoProducts = listOf(
        Product(
            id = "prod_tomato_01",
            farmerId = "farmer_ramesh",
            farmerName = "Ramesh Reddy",
            farmName = "Reddy Organic & Natural Farms",
            name = "Country Desi Tomatoes (Vine-Ripened)",
            category = ProductCategory.VEGETABLES,
            imageUrl = "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=500&auto=format&fit=crop",
            unit = ProduceUnit.KG,
            quantityAvailable = 150.0,
            pricePerUnit = 32.0,
            minOrderQty = 2.0,
            harvestDate = "Harvested Today 6 AM",
            fulfillmentMethod = FulfillmentMethod.BOTH,
            location = "Guntur, AP",
            averageRating = 4.9,
            reviewCount = 8
        ),
        Product(
            id = "prod_chilli_02",
            farmerId = "farmer_ramesh",
            farmerName = "Ramesh Reddy",
            farmName = "Reddy Organic & Natural Farms",
            name = "Guntur Sannam Hot Chillies",
            category = ProductCategory.VEGETABLES,
            imageUrl = "https://images.unsplash.com/photo-1588252303782-cb80119abd6d?w=500&auto=format&fit=crop",
            unit = ProduceUnit.KG,
            quantityAvailable = 45.0,
            pricePerUnit = 85.0,
            minOrderQty = 1.0,
            harvestDate = "Sun-Dried Fresh",
            fulfillmentMethod = FulfillmentMethod.BOTH,
            location = "Guntur, AP",
            averageRating = 4.8,
            reviewCount = 5
        ),
        Product(
            id = "prod_onion_03",
            farmerId = "farmer_anitha",
            farmerName = "Anitha Rao",
            farmName = "Kakatiya Heritage Orchards",
            name = "Nasik Heritage Red Onions",
            category = ProductCategory.VEGETABLES,
            imageUrl = "https://images.unsplash.com/photo-1618512496248-a07fe83aa8cb?w=500&auto=format&fit=crop",
            unit = ProduceUnit.KG,
            quantityAvailable = 320.0,
            pricePerUnit = 28.0,
            minOrderQty = 5.0,
            harvestDate = "Cured & Stored Dry",
            fulfillmentMethod = FulfillmentMethod.BOTH,
            location = "Warangal, TG",
            averageRating = 4.7,
            reviewCount = 6
        ),
        Product(
            id = "prod_mango_04",
            farmerId = "farmer_ramesh",
            farmerName = "Ramesh Reddy",
            farmName = "Reddy Organic & Natural Farms",
            name = "Banganapalli Sweet Mangoes (Carbide Free)",
            category = ProductCategory.FRUITS,
            imageUrl = "https://images.unsplash.com/photo-1553279768-865429fa0078?w=500&auto=format&fit=crop",
            unit = ProduceUnit.DOZEN,
            quantityAvailable = 40.0,
            pricePerUnit = 380.0,
            minOrderQty = 1.0,
            harvestDate = "Tree Ripened",
            fulfillmentMethod = FulfillmentMethod.PICKUP_ONLY,
            location = "Guntur, AP",
            averageRating = 5.0,
            reviewCount = 7
        ),
        Product(
            id = "prod_banana_05",
            farmerId = "farmer_anitha",
            farmerName = "Anitha Rao",
            farmName = "Kakatiya Heritage Orchards",
            name = "Yelakki Sweet Baby Bananas",
            category = ProductCategory.FRUITS,
            imageUrl = "https://images.unsplash.com/photo-1571771894821-ce9b6c11b08e?w=500&auto=format&fit=crop",
            unit = ProduceUnit.DOZEN,
            quantityAvailable = 60.0,
            pricePerUnit = 65.0,
            minOrderQty = 2.0,
            harvestDate = "Harvested Yesterday",
            fulfillmentMethod = FulfillmentMethod.BOTH,
            location = "Warangal, TG",
            averageRating = 4.8,
            reviewCount = 4
        ),
        Product(
            id = "prod_brinjal_06",
            farmerId = "farmer_ramesh",
            farmerName = "Ramesh Reddy",
            farmName = "Reddy Organic & Natural Farms",
            name = "Striped Krishna Purple Brinjals",
            category = ProductCategory.VEGETABLES,
            imageUrl = "https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=500&auto=format&fit=crop",
            unit = ProduceUnit.KG,
            quantityAvailable = 90.0,
            pricePerUnit = 35.0,
            minOrderQty = 1.0,
            harvestDate = "Harvested This Morning",
            fulfillmentMethod = FulfillmentMethod.BOTH,
            location = "Guntur, AP",
            averageRating = 4.6,
            reviewCount = 3
        )
    )

    val demoReviews = listOf(
        Review(
            id = "rev_001",
            orderId = "order_1001",
            productId = "prod_tomato_01",
            productName = "Country Desi Tomatoes (Vine-Ripened)",
            farmerId = "farmer_ramesh",
            farmerName = "Ramesh Reddy",
            buyerId = "buyer_priya",
            buyerName = "Priya Nambiar",
            rating = 5,
            comment = "Outstanding sweetness and aroma! Nothing like the sour greenhouse ones in retail marts. Making rasam was pure nostalgia.",
            verifiedPurchase = true,
            farmerReply = "Thank you Priya garu! We pick at 5 AM right when the dew settles for optimal natural sweetness."
        ),
        Review(
            id = "rev_002",
            orderId = "order_1002",
            productId = "prod_chilli_02",
            productName = "Guntur Sannam Hot Chillies",
            farmerId = "farmer_ramesh",
            farmerName = "Ramesh Reddy",
            buyerId = "buyer_swagath",
            buyerName = "Swagath Grand Hotel (Kiran)",
            rating = 5,
            comment = "Authentic Guntur pungency and vibrant natural color. Our Andhra biryani spice blend came out exceptional.",
            verifiedPurchase = true,
            farmerReply = "Honored to supply Swagath Grand kitchen! We dry these on clean raised mesh."
        ),
        Review(
            id = "rev_003",
            orderId = "order_1003",
            productId = "prod_onion_03",
            productName = "Nasik Heritage Red Onions",
            farmerId = "farmer_anitha",
            farmerName = "Anitha Rao",
            buyerId = "buyer_priya",
            buyerName = "Priya Nambiar",
            rating = 5,
            comment = "Very firm, thin-skinned and zero rotting bulbs. Direct farm packing makes a huge difference in freshness.",
            verifiedPurchase = true,
            farmerReply = "Thank you sister! We cure them for 10 days in well-ventilated dry sheds."
        ),
        Review(
            id = "rev_004",
            orderId = "order_1004",
            productId = "prod_mango_04",
            productName = "Banganapalli Sweet Mangoes (Carbide Free)",
            farmerId = "farmer_ramesh",
            farmerName = "Ramesh Reddy",
            buyerId = "buyer_priya",
            buyerName = "Priya Nambiar",
            rating = 5,
            comment = "Pure nectar! You can tell right away that no chemical carbide was touched. The fragrance filled our whole home.",
            verifiedPurchase = true,
            farmerReply = "Grass-wrapped ripening only! So glad the family loved them."
        ),
        Review(
            id = "rev_005",
            orderId = "order_1005",
            productId = "prod_banana_05",
            productName = "Yelakki Sweet Baby Bananas",
            farmerId = "farmer_anitha",
            farmerName = "Anitha Rao",
            buyerId = "buyer_swagath",
            buyerName = "Swagath Grand Hotel (Kiran)",
            rating = 4,
            comment = "Uniform sweetness and healthy stems. Perfect for morning buffet fruit platters.",
            verifiedPurchase = true,
            farmerReply = "Glad to serve! We prune carefully to preserve uniform sizing."
        )
    )

    fun createInitialOrders(): List<Order> {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3600000L
        val twoDaysAgo = now - 172800000L

        return listOf(
            Order(
                id = "order_1001",
                buyerId = "buyer_priya",
                buyerName = "Priya Nambiar",
                buyerPhone = "9848011223",
                farmerId = "farmer_ramesh",
                farmerName = "Ramesh Reddy",
                items = listOf(
                    OrderItemSnapshot(
                        productId = "prod_tomato_01",
                        productName = "Country Desi Tomatoes (Vine-Ripened)",
                        quantity = 5.0,
                        unit = ProduceUnit.KG,
                        unitPriceSnapshot = 32.0
                    )
                ),
                totalAmount = 160.0,
                fulfillmentChoice = FulfillmentChoice.DELIVERY,
                deliveryAddress = "Flat 402, Green Meadows, Banjara Hills Road 12, Hyderabad",
                paymentMethod = PaymentMethod.SIMULATED_UPI,
                paymentStatus = PaymentStatus.PAID_SIMULATED,
                transactionReference = "UPI/DEMO/839210928",
                paymentTimestampMillis = twoDaysAgo,
                hasReviewed = true,
                status = OrderStatus.COMPLETED,
                statusHistory = listOf(
                    OrderStatusHistoryEntry(OrderStatus.REQUESTED, twoDaysAgo, "Priya Nambiar (BUYER)", "Order placed"),
                    OrderStatusHistoryEntry(OrderStatus.ACCEPTED, twoDaysAgo + 1800000, "Ramesh Reddy (FARMER)", "Stock reserved and confirmed"),
                    OrderStatusHistoryEntry(OrderStatus.READY, twoDaysAgo + 7200000, "Ramesh Reddy (FARMER)", "Packed fresh for dispatch"),
                    OrderStatusHistoryEntry(OrderStatus.COMPLETED, twoDaysAgo + 14400000, "Priya Nambiar (BUYER)", "Order received and confirmed")
                ),
                createdAtMillis = twoDaysAgo,
                updatedAtMillis = twoDaysAgo + 14400000
            ),
            Order(
                id = "order_1002",
                buyerId = "buyer_swagath",
                buyerName = "Swagath Grand Hotel (Kiran)",
                buyerPhone = "9849022334",
                farmerId = "farmer_ramesh",
                farmerName = "Ramesh Reddy",
                items = listOf(
                    OrderItemSnapshot(
                        productId = "prod_chilli_02",
                        productName = "Guntur Sannam Hot Chillies",
                        quantity = 10.0,
                        unit = ProduceUnit.KG,
                        unitPriceSnapshot = 85.0
                    )
                ),
                totalAmount = 850.0,
                fulfillmentChoice = FulfillmentChoice.PICKUP,
                deliveryAddress = null,
                paymentMethod = PaymentMethod.CASH_ON_PICKUP,
                paymentStatus = PaymentStatus.CASH_DUE,
                transactionReference = null,
                hasReviewed = true,
                status = OrderStatus.COMPLETED,
                statusHistory = listOf(
                    OrderStatusHistoryEntry(OrderStatus.REQUESTED, twoDaysAgo + 20000000, "Swagath Grand Hotel (BUYER)", "Commercial kitchen requisition"),
                    OrderStatusHistoryEntry(OrderStatus.ACCEPTED, twoDaysAgo + 22000000, "Ramesh Reddy (FARMER)", "Accepted order"),
                    OrderStatusHistoryEntry(OrderStatus.READY, twoDaysAgo + 25000000, "Ramesh Reddy (FARMER)", "Packed and ready at farm counter"),
                    OrderStatusHistoryEntry(OrderStatus.COMPLETED, twoDaysAgo + 30000000, "Ramesh Reddy (FARMER)", "Payment of ₹850 received in cash")
                ),
                createdAtMillis = twoDaysAgo + 20000000,
                updatedAtMillis = twoDaysAgo + 30000000
            ),
            Order(
                id = "order_1003",
                buyerId = "buyer_priya",
                buyerName = "Priya Nambiar",
                buyerPhone = "9848011223",
                farmerId = "farmer_anitha",
                farmerName = "Anitha Rao",
                items = listOf(
                    OrderItemSnapshot(
                        productId = "prod_onion_03",
                        productName = "Nasik Heritage Red Onions",
                        quantity = 10.0,
                        unit = ProduceUnit.KG,
                        unitPriceSnapshot = 28.0
                    )
                ),
                totalAmount = 280.0,
                fulfillmentChoice = FulfillmentChoice.DELIVERY,
                deliveryAddress = "Flat 402, Green Meadows, Banjara Hills Road 12, Hyderabad",
                paymentMethod = PaymentMethod.SIMULATED_CARD,
                paymentStatus = PaymentStatus.PAID_SIMULATED,
                transactionReference = "CARD/DEMO/VISA-9941",
                paymentTimestampMillis = oneHourAgo,
                hasReviewed = false,
                status = OrderStatus.ACCEPTED,
                statusHistory = listOf(
                    OrderStatusHistoryEntry(OrderStatus.REQUESTED, oneHourAgo - 1800000, "Priya Nambiar (BUYER)", "Household order"),
                    OrderStatusHistoryEntry(OrderStatus.ACCEPTED, oneHourAgo, "Anitha Rao (FARMER)", "Harvesting and sorting right away")
                ),
                createdAtMillis = oneHourAgo - 1800000,
                updatedAtMillis = oneHourAgo
            ),
            Order(
                id = "order_1004",
                buyerId = "buyer_priya",
                buyerName = "Priya Nambiar",
                buyerPhone = "9848011223",
                farmerId = "farmer_ramesh",
                farmerName = "Ramesh Reddy",
                items = listOf(
                    OrderItemSnapshot(
                        productId = "prod_tomato_01",
                        productName = "Country Desi Tomatoes (Vine-Ripened)",
                        quantity = 3.0,
                        unit = ProduceUnit.KG,
                        unitPriceSnapshot = 32.0
                    )
                ),
                totalAmount = 96.0,
                fulfillmentChoice = FulfillmentChoice.PICKUP,
                deliveryAddress = null,
                paymentMethod = PaymentMethod.CASH_ON_PICKUP,
                paymentStatus = PaymentStatus.CASH_DUE,
                hasReviewed = false,
                status = OrderStatus.REQUESTED,
                statusHistory = listOf(
                    OrderStatusHistoryEntry(OrderStatus.REQUESTED, now - 600000, "Priya Nambiar (BUYER)", "Awaiting farmer confirmation")
                ),
                createdAtMillis = now - 600000,
                updatedAtMillis = now - 600000
            )
        )
    }
}
