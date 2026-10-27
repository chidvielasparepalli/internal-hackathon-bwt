package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.Role
import com.example.ui.admin.AdminModerationScreen
import com.example.ui.auth.AuthScreen
import com.example.ui.buyer.BuyerDashboardScreen
import com.example.ui.buyer.MarketplaceScreen
import com.example.ui.buyer.OrderReceiptDialog
import com.example.ui.buyer.ProductDetailScreen
import com.example.ui.farmer.FarmerWorkspaceScreen
import com.example.ui.landing.LandingScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.HarvestLinkTheme
import com.example.ui.theme.TerracottaAccent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestLinkApp(
    viewModel: HarvestLinkViewModel = viewModel()
) {
    HarvestLinkTheme {
        val currentUser by viewModel.currentUser.collectAsState()
        val allUsers by viewModel.allUsers.collectAsState()
        val selectedProduct by viewModel.selectedProduct.collectAsState()
        val isGuestMode by viewModel.isGuestMode.collectAsState()
        val selectedReceiptOrder by viewModel.selectedReceiptOrder.collectAsState()

        val snackbarHostState = remember { SnackbarHostState() }

        // Navigation state
        var authMode by remember { mutableStateOf<String?>(null) } // null: landing/normal, "signin", "signup"
        var buyerTab by remember { mutableIntStateOf(0) } // 0: Market, 1: Orders, 2: Profile
        var farmerTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Orders, 2: Reviews, 3: Profile
        var showProfileMenu by remember { mutableStateOf(false) }

        // Toast notifications
        LaunchedEffect(Unit) {
            viewModel.userMessage.collectLatest { msg ->
                snackbarHostState.showSnackbar(msg)
            }
        }

        // Global Order Receipt Dialog
        if (selectedReceiptOrder != null) {
            OrderReceiptDialog(
                order = selectedReceiptOrder!!,
                onDismiss = { viewModel.showOrderReceipt(null) }
            )
        }

        if (currentUser == null) {
            if (!isGuestMode) {
                if (authMode == null) {
                    // 1. Redesigned Editorial Landing Screen
                    LandingScreen(
                        viewModel = viewModel,
                        onExploreAsGuest = {
                            viewModel.enterGuestMode()
                        },
                        onSignInClick = {
                            authMode = "signin"
                        },
                        onCreateAccountClick = {
                            authMode = "signup"
                        },
                        onProductClick = { prod ->
                            viewModel.enterGuestMode()
                            viewModel.selectProduct(prod)
                        }
                    )
                } else {
                    // 2. Real Account Creation / Sign In Screen
                    AuthScreen(
                        viewModel = viewModel,
                        initialIsSignUp = (authMode == "signup"),
                        onBack = { authMode = null },
                        onExploreAsGuest = {
                            authMode = null
                            viewModel.enterGuestMode()
                        }
                    )
                }
            } else {
                // 3. Guest Mode (Read-only browsing of Marketplace)
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    topBar = {
                        TopAppBar(
                            title = {
                                Column {
                                    Text(
                                        text = "HarvestLink",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Guest Mode • Read-Only Browse",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                }
                            },
                            actions = {
                                Button(
                                    onClick = {
                                        viewModel.exitGuestMode()
                                        authMode = "signin"
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = TerracottaAccent,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.padding(end = 8.dp).testTag("guest_signin_top_button")
                                ) {
                                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sign In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = ForestGreenPrimary,
                                titleContentColor = Color.White,
                                actionIconContentColor = Color.White
                            )
                        )
                    },
                    bottomBar = {
                        if (selectedProduct == null) {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = ForestGreenPrimary
                            ) {
                                NavigationBarItem(
                                    selected = true,
                                    onClick = { },
                                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Marketplace") },
                                    label = { Text("Market") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        indicatorColor = ForestGreenPrimary,
                                        selectedTextColor = ForestGreenPrimary
                                    ),
                                    modifier = Modifier.testTag("nav_guest_marketplace")
                                )
                                NavigationBarItem(
                                    selected = false,
                                    onClick = {
                                        viewModel.exitGuestMode()
                                        authMode = "signin"
                                    },
                                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Sign In / Join") },
                                    label = { Text("Sign In / Join") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        indicatorColor = ForestGreenPrimary,
                                        selectedTextColor = ForestGreenPrimary
                                    ),
                                    modifier = Modifier.testTag("nav_guest_signin")
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    if (selectedProduct != null) {
                        ProductDetailScreen(
                            product = selectedProduct!!,
                            viewModel = viewModel,
                            onBack = { viewModel.selectProduct(null) },
                            onSelectOtherProduct = { newProd -> viewModel.selectProduct(newProd) },
                            onPromptSignIn = {
                                viewModel.exitGuestMode()
                                authMode = "signin"
                            },
                            modifier = Modifier.padding(paddingValues)
                        )
                    } else {
                        MarketplaceScreen(
                            viewModel = viewModel,
                            onProductClick = { prod -> viewModel.selectProduct(prod) },
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        } else {
            val user = currentUser!!

            Scaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (user.role) {
                                            Role.BUYER -> Icons.Default.Storefront
                                            Role.FARMER -> Icons.Default.Agriculture
                                            Role.ADMIN -> Icons.Default.Security
                                        },
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "HarvestLink",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${user.name} • ${user.role.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        },
                        actions = {
                            // Quick Switch Account for hackathon testing
                            Box {
                                IconButton(
                                    onClick = { showProfileMenu = true },
                                    modifier = Modifier.testTag("switch_user_menu_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = "Switch Account",
                                        tint = Color.White
                                    )
                                }
                                DropdownMenu(
                                    expanded = showProfileMenu,
                                    onDismissRequest = { showProfileMenu = false }
                                ) {
                                    Text(
                                        text = "Quick Switch Account:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                    allUsers.forEach { u ->
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text("${u.name} (${u.role.name})", fontWeight = FontWeight.Medium)
                                                    Text(u.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                                }
                                            },
                                            onClick = {
                                                showProfileMenu = false
                                                viewModel.loginAs(u)
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Sign Out", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = {
                                            Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                        },
                                        onClick = {
                                            showProfileMenu = false
                                            viewModel.logout()
                                        }
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = ForestGreenPrimary,
                            titleContentColor = Color.White,
                            actionIconContentColor = Color.White
                        )
                    )
                },
                bottomBar = {
                    // Role-specific bottom bars
                    when (user.role) {
                        Role.BUYER -> {
                            // Buyer bottom bar: Market, Orders, Profile
                            if (selectedProduct == null) {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = ForestGreenPrimary
                                ) {
                                    NavigationBarItem(
                                        selected = buyerTab == 0,
                                        onClick = { buyerTab = 0 },
                                        icon = { Icon(Icons.Default.Storefront, contentDescription = "Market") },
                                        label = { Text("Market") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            indicatorColor = ForestGreenPrimary,
                                            selectedTextColor = ForestGreenPrimary
                                        ),
                                        modifier = Modifier.testTag("nav_marketplace")
                                    )
                                    NavigationBarItem(
                                        selected = buyerTab == 1,
                                        onClick = { buyerTab = 1 },
                                        icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                                        label = { Text("Orders") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            indicatorColor = ForestGreenPrimary,
                                            selectedTextColor = ForestGreenPrimary
                                        ),
                                        modifier = Modifier.testTag("nav_orders")
                                    )
                                    NavigationBarItem(
                                        selected = buyerTab == 2,
                                        onClick = { buyerTab = 2 },
                                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                        label = { Text("Profile") },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color.White,
                                            indicatorColor = ForestGreenPrimary,
                                            selectedTextColor = ForestGreenPrimary
                                        ),
                                        modifier = Modifier.testTag("nav_profile")
                                    )
                                }
                            }
                        }
                        Role.FARMER -> {
                            // Farmer bottom bar: Overview, Orders, Reviews, Profile
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = ForestGreenPrimary
                            ) {
                                NavigationBarItem(
                                    selected = farmerTab == 0,
                                    onClick = { farmerTab = 0 },
                                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                                    label = { Text("Overview") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        indicatorColor = ForestGreenPrimary,
                                        selectedTextColor = ForestGreenPrimary
                                    ),
                                    modifier = Modifier.testTag("nav_farmer_overview")
                                )
                                NavigationBarItem(
                                    selected = farmerTab == 1,
                                    onClick = { farmerTab = 1 },
                                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Orders") },
                                    label = { Text("Orders") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        indicatorColor = ForestGreenPrimary,
                                        selectedTextColor = ForestGreenPrimary
                                    ),
                                    modifier = Modifier.testTag("nav_farmer_orders")
                                )
                                NavigationBarItem(
                                    selected = farmerTab == 2,
                                    onClick = { farmerTab = 2 },
                                    icon = { Icon(Icons.Default.RateReview, contentDescription = "Reviews") },
                                    label = { Text("Reviews") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        indicatorColor = ForestGreenPrimary,
                                        selectedTextColor = ForestGreenPrimary
                                    ),
                                    modifier = Modifier.testTag("nav_farmer_reviews")
                                )
                                NavigationBarItem(
                                    selected = farmerTab == 3,
                                    onClick = { farmerTab = 3 },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                    label = { Text("Profile") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        indicatorColor = ForestGreenPrimary,
                                        selectedTextColor = ForestGreenPrimary
                                    ),
                                    modifier = Modifier.testTag("nav_farmer_profile")
                                )
                            }
                        }
                        Role.ADMIN -> {
                            // Admin has moderation panel tabs internally
                        }
                    }
                }
            ) { paddingValues ->
                // STRICT ROLE-BASED HOME SCREEN ENFORCEMENT
                when (user.role) {
                    Role.BUYER -> {
                        if (selectedProduct != null) {
                            ProductDetailScreen(
                                product = selectedProduct!!,
                                viewModel = viewModel,
                                onBack = { viewModel.selectProduct(null) },
                                onSelectOtherProduct = { newProd -> viewModel.selectProduct(newProd) },
                                onPromptSignIn = { },
                                modifier = Modifier.padding(paddingValues)
                            )
                        } else when (buyerTab) {
                            0 -> MarketplaceScreen(
                                viewModel = viewModel,
                                onProductClick = { prod -> viewModel.selectProduct(prod) },
                                modifier = Modifier.padding(paddingValues)
                            )
                            1 -> BuyerDashboardScreen(
                                viewModel = viewModel,
                                onBrowseMarketplace = { buyerTab = 0 },
                                modifier = Modifier.padding(paddingValues)
                            )
                            2 -> ProfileScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                    }
                    Role.FARMER -> {
                        when (farmerTab) {
                            3 -> ProfileScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(paddingValues)
                            )
                            else -> FarmerWorkspaceScreen(
                                viewModel = viewModel,
                                modifier = Modifier.padding(paddingValues)
                            )
                        }
                    }
                    Role.ADMIN -> {
                        AdminModerationScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}
