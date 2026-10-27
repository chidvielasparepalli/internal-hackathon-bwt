package com.example.ui.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Product
import com.example.ui.HarvestLinkViewModel
import com.example.ui.common.DisclaimerBanner
import com.example.ui.common.ProduceImage
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TerracottaAccent
import kotlinx.coroutines.delay

data class CarouselSlide(
    val title: String,
    val subtitle: String,
    val quote: String,
    val tag: String,
    val imageUrl: String,
    val icon: ImageVector
)

@Composable
fun LandingScreen(
    viewModel: HarvestLinkViewModel,
    onExploreAsGuest: () -> Unit,
    onSignInClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val allProducts by viewModel.allProducts.collectAsState()
    val featuredProducts = remember(allProducts) {
        allProducts.filter { it.isActive && !it.flagged }.take(6)
    }

    val slides = remember {
        listOf(
            CarouselSlide(
                title = "Ramesh Reddy • Guntur, AP",
                subtitle = "Reddy Organic Farms (12 Acres)",
                quote = "\"Selling my Guntur chillies directly keeps pricing fair and gives buyers true farm-fresh quality.\"",
                tag = "Farmer Story",
                imageUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=500&auto=format&fit=crop",
                icon = Icons.Default.Agriculture
            ),
            CarouselSlide(
                title = "Priya Nambiar • Hyderabad",
                subtitle = "Household Buyer (Banjara Hills)",
                quote = "\"Tomatoes harvested at dawn taste entirely different. Every order has a crystal clear status.\"",
                tag = "Buyer Experience",
                imageUrl = "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=500&auto=format&fit=crop",
                icon = Icons.Default.CheckCircle
            ),
            CarouselSlide(
                title = "Atomic Stock Reservation",
                subtitle = "Guaranteed produce availability",
                quote = "\"When you submit an order, stock is reserved in one transaction. No over-ordering ever.\"",
                tag = "Feature Highlight",
                imageUrl = "https://images.unsplash.com/photo-1588252303782-cb80119abd6d?w=500&auto=format&fit=crop",
                icon = Icons.Default.Inventory
            ),
            CarouselSlide(
                title = "Swagath Grand Hotel • Vizag",
                subtitle = "Restaurant Partner (Bulk Sourcing)",
                quote = "\"Direct sourcing of Krishna brinjals and okra saved our kitchen 20% while delighting dining guests.\"",
                tag = "Restaurant Partner",
                imageUrl = "https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=500&auto=format&fit=crop",
                icon = Icons.Default.ReceiptLong
            ),
            CarouselSlide(
                title = "Anitha Rao • Warangal, TG",
                subtitle = "Kakatiya Heritage Orchards",
                quote = "\"Tree-ripened sweet lime and robusta bananas. Direct buyers order with transparent pickup schedules.\"",
                tag = "Farmer Story",
                imageUrl = "https://images.unsplash.com/photo-1534080564583-6be75777b70a?w=500&auto=format&fit=crop",
                icon = Icons.Default.Agriculture
            ),
            CarouselSlide(
                title = "Zero Risk Demo Payments",
                subtitle = "Simulated UPI, Cards & Cash on Pickup",
                quote = "\"Try full checkout with simulated UPI and card payments. No real financial credentials collected.\"",
                tag = "Feature Highlight",
                imageUrl = "https://images.unsplash.com/photo-1553279768-865429fa0078?w=500&auto=format&fit=crop",
                icon = Icons.Default.Shield
            )
        )
    }

    var currentSlideIndex by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(isPaused) {
        if (!isPaused) {
            while (true) {
                delay(4000)
                currentSlideIndex = (currentSlideIndex + 1) % slides.size
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ForestGreenPrimary)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HarvestLink",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Farmers sell directly. Buyers order with confidence. Every order has a clear status.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = onSignInClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = ForestGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("landing_signin_button")
                        ) {
                            Text("Sign in", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Button(
                            onClick = onCreateAccountClick,
                            colors = ButtonDefaults.buttonColors(containerColor = TerracottaAccent, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f).height(46.dp).testTag("landing_create_account_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create account", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onExploreAsGuest,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp).testTag("landing_explore_guest_button")
                    ) {
                        Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Explore as guest (Browse produce read-only)", fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stories & Highlights", style = MaterialTheme.typography.titleLarge, color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp)) {
                        Text(
                            text = "Sample stories from the demo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                val currentSlide = slides[currentSlideIndex]
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { isPaused = !isPaused }.testTag("landing_carousel_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(150.dp)) {
                            ProduceImage(currentSlide.imageUrl, currentSlide.title, Modifier.fillMaxSize(), ContentScale.Crop)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ForestGreenPrimary.copy(alpha = 0.85f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(currentSlide.tag, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(currentSlide.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            Text(currentSlide.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(currentSlide.quote, style = MaterialTheme.typography.bodyMedium, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    slides.forEachIndexed { i, _ ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (i == currentSlideIndex) 9.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (i == currentSlideIndex) ForestGreenPrimary else Color.LightGray)
                                .clickable { currentSlideIndex = i }
                        )
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Fresh this week", style = MaterialTheme.typography.titleLarge, color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(featuredProducts, key = { it.id }) { product ->
                        Card(
                            modifier = Modifier.width(150.dp).clickable { onProductClick(product) }.testTag("landing_product_${product.id}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column {
                                Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                                    ProduceImage(product.imageUrl, product.name, Modifier.fillMaxSize())
                                    Box(
                                        modifier = Modifier.align(Alignment.TopStart).padding(4.dp).clip(RoundedCornerShape(3.dp)).background(Color.Black.copy(alpha = 0.6f)).padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("★ ${product.averageRating}", color = Color(0xFFFFD700), style = MaterialTheme.typography.labelSmall, fontSize = 9.sp)
                                    }
                                }
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(product.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                                    Text("₹${product.pricePerUnit.toInt()}/${product.unit.label}", style = MaterialTheme.typography.bodyMedium, color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                                    Text("${product.farmerName} • ${product.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
                Text("How HarvestLink Works", style = MaterialTheme.typography.titleLarge, color = ForestGreenPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("1. Harvest", fontWeight = FontWeight.Bold, color = ForestGreenPrimary, fontSize = 12.sp)
                            Text("Farmers list live produce with clear harvest dates.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("2. Atomic Order", fontWeight = FontWeight.Bold, color = ForestGreenPrimary, fontSize = 12.sp)
                            Text("Stock is reserved in one transaction immediately.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Card(modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("3. Clear Status", fontWeight = FontWeight.Bold, color = ForestGreenPrimary, fontSize = 12.sp)
                            Text("Track from Requested to Ready to Completed.", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            DisclaimerBanner(
                message = "Student hackathon prototype (HACK-P309). Delivery and payment options are simulated for demo purposes only; no real money moves.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
