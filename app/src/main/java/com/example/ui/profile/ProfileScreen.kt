package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FeedbackCategory
import com.example.model.Role
import com.example.ui.HarvestLinkViewModel
import com.example.ui.common.ConfirmationDialog
import com.example.ui.common.ProduceImage
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TerracottaAccent

@Composable
fun ProfileScreen(
    viewModel: HarvestLinkViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val farmerProfiles by viewModel.farmerProfiles.collectAsState()
    val user = currentUser ?: return

    val farmerProfile = farmerProfiles.find { it.id == user.id }

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showFeedbackDialog by remember { mutableStateOf(false) }
    var showResetConfirmation by remember { mutableStateOf(false) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // User Avatar and Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("profile_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(2.dp, ForestGreenPrimary, CircleShape)
                    ) {
                        if (user.profilePhotoUrl != null) {
                            ProduceImage(imageUrl = user.profilePhotoUrl, contentDescription = user.name, modifier = Modifier.fillMaxSize())
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(ForestGreenPrimary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = ForestGreenPrimary, modifier = Modifier.size(36.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(user.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                    Text("${user.role.name} • ${user.email}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Phone: +91 ${user.phone}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (farmerProfile != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(color = ForestGreenPrimary.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD9822B), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rating: ${farmerProfile.averageRating} (${farmerProfile.reviewCount} verified reviews)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = ForestGreenPrimary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("profile_edit_button")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Edit Profile Details", fontSize = 13.sp)
                    }
                }
            }
        }

        // Details Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Account Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    ProfileInfoRow(label = "Location", value = user.location)

                    if (user.role == Role.BUYER) {
                        user.buyerType?.let { ProfileInfoRow(label = "Buyer Type", value = it.displayName) }
                        user.deliveryAddress?.let { ProfileInfoRow(label = "Delivery Address", value = it) }
                    } else if (user.role == Role.FARMER && farmerProfile != null) {
                        ProfileInfoRow(label = "Farm Name", value = farmerProfile.farmName)
                        ProfileInfoRow(label = "Farm Acreage", value = farmerProfile.acreage)
                        ProfileInfoRow(label = "Experience", value = "${farmerProfile.experienceYears} Years in Organic Farming")
                        ProfileInfoRow(label = "Main Crops", value = farmerProfile.mainCrops.joinToString(", "))
                        ProfileInfoRow(label = "Farm Story", value = farmerProfile.description)
                    }
                }
            }
        }

        // Actions & Demo Settings Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("App Feedback & Demo Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedButton(
                        onClick = { showFeedbackDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("profile_feedback_button")
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send App Feedback or Bug Report", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { showResetConfirmation = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TerracottaAccent),
                        modifier = Modifier.fillMaxWidth().testTag("profile_reset_demo_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Demo Data (Hackathon Prototype)", fontSize = 13.sp)
                    }

                    Button(
                        onClick = { showLogoutConfirmation = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth().testTag("profile_logout_button")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var editName by remember { mutableStateOf(user.name) }
        var editPhone by remember { mutableStateOf(user.phone) }
        var editLocation by remember { mutableStateOf(user.location) }
        var editAddress by remember { mutableStateOf(user.deliveryAddress ?: "") }
        var editFarmName by remember { mutableStateOf(farmerProfile?.farmName ?: "") }
        var editFarmStory by remember { mutableStateOf(farmerProfile?.description ?: "") }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile Details", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(value = editLocation, onValueChange = { editLocation = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    if (user.role == Role.BUYER) {
                        OutlinedTextField(value = editAddress, onValueChange = { editAddress = it }, label = { Text("Delivery Address") }, modifier = Modifier.fillMaxWidth())
                    } else if (user.role == Role.FARMER) {
                        OutlinedTextField(value = editFarmName, onValueChange = { editFarmName = it }, label = { Text("Farm Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(value = editFarmStory, onValueChange = { editFarmStory = it }, label = { Text("Farm Story") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateUserProfile(
                            name = editName,
                            phone = editPhone,
                            location = editLocation,
                            deliveryAddress = if (user.role == Role.BUYER) editAddress else null,
                            profilePhotoUrl = user.profilePhotoUrl,
                            farmName = if (user.role == Role.FARMER) editFarmName else null,
                            farmStory = if (user.role == Role.FARMER) editFarmStory else null,
                            onSuccess = { showEditProfileDialog = false }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                ) { Text("Save Changes") }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancel") }
            }
        )
    }

    // App Feedback Dialog
    if (showFeedbackDialog) {
        var selectedCat by remember { mutableStateOf(FeedbackCategory.GENERAL) }
        var messageText by remember { mutableStateOf("") }
        var screenshotRef by remember { mutableStateOf("HACK-P309 Evaluation") }

        AlertDialog(
            onDismissRequest = { showFeedbackDialog = false },
            title = { Text("Send Prototype Feedback", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FeedbackCategory.values().take(3).forEach { cat ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { selectedCat = cat }, verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedCat == cat, onClick = { selectedCat = cat })
                            Text(cat.label, fontSize = 12.sp)
                        }
                    }
                    OutlinedTextField(value = messageText, onValueChange = { messageText = it }, label = { Text("Your Feedback or Bug Report") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitAppFeedback(selectedCat, messageText, 5, screenshotRef, onSuccess = { showFeedbackDialog = false })
                    },
                    enabled = messageText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                ) { Text("Send Feedback") }
            },
            dismissButton = {
                TextButton(onClick = { showFeedbackDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Reset Confirmation Dialog
    if (showResetConfirmation) {
        ConfirmationDialog(
            title = "Reset Demo Data?",
            message = "This will restore all produce inventory, stock levels, orders, and demo reviews to the initial hackathon prototype state.",
            confirmButtonText = "Reset Data",
            isDestructive = true,
            onConfirm = {
                viewModel.resetDemoData()
                showResetConfirmation = false
            },
            onDismiss = { showResetConfirmation = false }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirmation) {
        ConfirmationDialog(
            title = "Sign Out?",
            message = "Are you sure you want to sign out of your HarvestLink account?",
            confirmButtonText = "Sign Out",
            isDestructive = true,
            onConfirm = {
                viewModel.logout()
                showLogoutConfirmation = false
            },
            onDismiss = { showLogoutConfirmation = false }
        )
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Normal)
    }
}
