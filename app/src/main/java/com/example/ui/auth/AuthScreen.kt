package com.example.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.logic.PasswordStrength
import com.example.logic.StockOrderEngine
import com.example.model.BuyerType
import com.example.model.Role
import com.example.model.User
import com.example.ui.HarvestLinkViewModel
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TerracottaAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: HarvestLinkViewModel,
    initialIsSignUp: Boolean = false,
    onBack: () -> Unit,
    onExploreAsGuest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSignUp by remember { mutableStateOf(initialIsSignUp) }
    val allUsers by viewModel.allUsers.collectAsState()

    // Sign in state
    var signInEmail by remember { mutableStateOf("") }
    var signInPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Sign up state
    var selectedRole by remember { mutableStateOf(Role.BUYER) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var buyerType by remember { mutableStateOf(BuyerType.HOUSEHOLD) }
    var deliveryAddress by remember { mutableStateOf("") }
    var farmName by remember { mutableStateOf("") }
    var farmStory by remember { mutableStateOf("") }
    var mainCropsText by remember { mutableStateOf("Tomatoes, Chillies, Mangoes") }

    var showDemoAccounts by remember { mutableStateOf(false) }

    val passwordStrength = remember(password) {
        StockOrderEngine.calculatePasswordStrength(password)
    }

    val validationResult = remember(
        fullName, email, phone, password, confirmPassword, location, selectedRole, farmName, deliveryAddress, allUsers
    ) {
        val existingEmails = allUsers.map { it.email }
        StockOrderEngine.validateSignUp(
            fullName = fullName,
            email = email,
            phone = phone,
            password = password,
            confirmPassword = confirmPassword,
            location = location,
            role = selectedRole,
            farmName = farmName,
            buyerDeliveryAddress = deliveryAddress,
            existingEmails = existingEmails
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSignUp) "Create Account" else "Sign In", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("auth_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ForestGreenPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                // Mode switcher tab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isSignUp) ForestGreenPrimary else Color.Transparent)
                            .clickable { isSignUp = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Sign In",
                            fontWeight = FontWeight.Bold,
                            color = if (!isSignUp) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSignUp) ForestGreenPrimary else Color.Transparent)
                            .clickable { isSignUp = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Create Account",
                            fontWeight = FontWeight.Bold,
                            color = if (isSignUp) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            if (!isSignUp) {
                // SIGN IN FORM
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Welcome Back", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                        Text("Sign in to your HarvestLink account to buy produce or manage farm inventory.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        OutlinedTextField(
                            value = signInEmail,
                            onValueChange = { signInEmail = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth().testTag("signin_email_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = signInPassword,
                            onValueChange = { signInPassword = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Toggle password visibility")
                                }
                            },
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("signin_password_input"),
                            singleLine = true
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = {
                                if (signInEmail.isNotBlank()) {
                                    // Simulated reset
                                }
                            }) {
                                Text("Forgot password?", color = TerracottaAccent, fontSize = 13.sp)
                            }
                        }

                        Button(
                            onClick = { viewModel.login(signInEmail, signInPassword) },
                            enabled = signInEmail.isNotBlank() && signInPassword.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("signin_submit_button")
                        ) {
                            Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            } else {
                // SIGN UP FORM
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Choose Your Role", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)

                        // Role Choice Cards (Buyer vs Farmer)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (selectedRole == Role.BUYER) 2.dp else 1.dp,
                                        color = if (selectedRole == Role.BUYER) ForestGreenPrimary else Color.LightGray,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedRole = Role.BUYER }
                                    .testTag("signup_role_buyer"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedRole == Role.BUYER) ForestGreenPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(if (selectedRole == Role.BUYER) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Storefront, contentDescription = null, tint = if (selectedRole == Role.BUYER) Color.White else ForestGreenPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Buyer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text("Household, store, kitchen", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (selectedRole == Role.FARMER) 2.dp else 1.dp,
                                        color = if (selectedRole == Role.FARMER) ForestGreenPrimary else Color.LightGray,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedRole = Role.FARMER }
                                    .testTag("signup_role_farmer"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedRole == Role.FARMER) ForestGreenPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(if (selectedRole == Role.FARMER) ForestGreenPrimary else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Agriculture, contentDescription = null, tint = if (selectedRole == Role.FARMER) Color.White else ForestGreenPrimary)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Farmer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                    Text("Sell harvest directly", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        // Common Fields
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("Full Name") },
                            isError = validationResult.nameError != null,
                            supportingText = { validationResult.nameError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            modifier = Modifier.fillMaxWidth().testTag("signup_name_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            isError = validationResult.emailError != null,
                            supportingText = { validationResult.emailError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            modifier = Modifier.fillMaxWidth().testTag("signup_email_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone Number (+91 10-digit)") },
                            prefix = { Text("+91 ") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            isError = validationResult.phoneError != null,
                            supportingText = { validationResult.phoneError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            modifier = Modifier.fillMaxWidth().testTag("signup_phone_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (min 8 characters)") },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = validationResult.passwordError != null,
                            supportingText = {
                                if (password.isNotEmpty()) {
                                    val meterColor = when (passwordStrength) {
                                        PasswordStrength.TOO_SHORT -> Color.Red
                                        PasswordStrength.WEAK -> Color(0xFFD9822B)
                                        PasswordStrength.MEDIUM -> Color(0xFF2E7D32)
                                        PasswordStrength.STRONG -> ForestGreenPrimary
                                    }
                                    Text("Strength: ${passwordStrength.label}", color = meterColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("signup_password_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = validationResult.confirmPasswordError != null,
                            supportingText = { validationResult.confirmPasswordError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            modifier = Modifier.fillMaxWidth().testTag("signup_confirm_password_input"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location (District, State)") },
                            placeholder = { Text("e.g. Guntur, Andhra Pradesh") },
                            isError = validationResult.locationError != null,
                            supportingText = { validationResult.locationError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                            modifier = Modifier.fillMaxWidth().testTag("signup_location_input"),
                            singleLine = true
                        )

                        // Role-specific fields
                        if (selectedRole == Role.BUYER) {
                            Text("Buyer Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BuyerType.values().take(3).forEach { type ->
                                    FilterChip(
                                        selected = buyerType == type,
                                        onClick = { buyerType = type },
                                        label = { Text(type.displayName, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = deliveryAddress,
                                onValueChange = { deliveryAddress = it },
                                label = { Text("Delivery Address") },
                                isError = validationResult.roleSpecificError != null,
                                supportingText = { validationResult.roleSpecificError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth().testTag("signup_buyer_address_input")
                            )
                        } else {
                            OutlinedTextField(
                                value = farmName,
                                onValueChange = { farmName = it },
                                label = { Text("Farm / Orchard Name") },
                                isError = validationResult.roleSpecificError != null,
                                supportingText = { validationResult.roleSpecificError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                                modifier = Modifier.fillMaxWidth().testTag("signup_farm_name_input"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = farmStory,
                                onValueChange = { farmStory = it },
                                label = { Text("Short Farm Story / Farming Practices") },
                                modifier = Modifier.fillMaxWidth().testTag("signup_farm_story_input")
                            )

                            OutlinedTextField(
                                value = mainCropsText,
                                onValueChange = { mainCropsText = it },
                                label = { Text("Main Crops Grown (comma-separated)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Button(
                            onClick = {
                                val crops = mainCropsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                viewModel.signUpUser(
                                    fullName = fullName,
                                    email = email,
                                    phone = phone,
                                    password = password,
                                    confirmPassword = confirmPassword,
                                    location = location,
                                    role = selectedRole,
                                    buyerType = if (selectedRole == Role.BUYER) buyerType else null,
                                    deliveryAddress = if (selectedRole == Role.BUYER) deliveryAddress else null,
                                    farmName = if (selectedRole == Role.FARMER) farmName else null,
                                    farmStory = if (selectedRole == Role.FARMER) farmStory else null,
                                    mainCrops = crops
                                )
                            },
                            enabled = validationResult.isValid,
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("signup_submit_button")
                        ) {
                            Text("Create ${if (selectedRole == Role.BUYER) "Buyer" else "Farmer"} Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // COLLAPSED DEMO ACCOUNTS SECTION (High-contrast, clearly readable buttons)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("demo_accounts_card"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { showDemoAccounts = !showDemoAccounts },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Try a demo account", fontWeight = FontWeight.Bold, color = ForestGreenPrimary, fontSize = 14.sp)
                            }
                            Icon(if (showDemoAccounts) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                        }

                        AnimatedVisibility(visible = showDemoAccounts) {
                            Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Select any role to test instant live prototype permissions:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                allUsers.forEach { user ->
                                    Surface(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("${user.role.name} • ${user.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                            }
                                            Button(
                                                onClick = { viewModel.loginAs(user) },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ForestGreenPrimary,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(34.dp).testTag("quick_login_${user.id}")
                                            ) {
                                                Text("Sign in", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
