package com.example.ui.buyer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.model.Order
import com.example.model.Review
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.TerracottaAccent

@Composable
fun OrderReviewDialog(
    order: Order,
    onSubmitReview: (Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val maxChars = 300

    val item = order.items.firstOrNull()
    val productName = item?.productName ?: "Produce"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RateReview, contentDescription = null, tint = ForestGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Write Farmer Review", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Farmer: ${order.farmerName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("✓ Verified Purchase • Order #${order.id}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1E6B3A), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                        }
                    }
                }

                Text("Your Rating", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { rating = i },
                            modifier = Modifier.testTag("star_rating_$i")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$i stars",
                                tint = if (i <= rating) Color(0xFFD9822B) else Color.LightGray,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = comment,
                    onValueChange = { if (it.length <= maxChars) comment = it },
                    label = { Text("How was the fresh produce quality?") },
                    placeholder = { Text("e.g. Crisp sweetness, perfect ripeness, exactly as promised...") },
                    modifier = Modifier.fillMaxWidth().testTag("review_comment_input"),
                    minLines = 3,
                    supportingText = {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text("${comment.length}/$maxChars chars", fontSize = 11.sp, color = if (comment.length == maxChars) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitReview(rating, comment) },
                enabled = rating in 1..5 && comment.trim().isNotEmpty() && comment.length <= maxChars,
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary),
                modifier = Modifier.testTag("submit_review_button")
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ReviewItemCard(
    review: Review,
    isFarmerOwner: Boolean = false,
    onReplySubmit: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showReplyField by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }

    Card(
        modifier = modifier.fillMaxWidth().testTag("review_card_${review.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(review.buyerName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (review.verifiedPurchase) {
                        Text("✓ Verified Purchase", style = MaterialTheme.typography.bodySmall, color = Color(0xFF1E6B3A), fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    }
                }

                Row {
                    for (i in 1..5) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i <= review.rating) Color(0xFFD9822B) else Color.LightGray,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(review.comment, style = MaterialTheme.typography.bodyMedium, fontSize = 13.sp)

            // Farmer Reply
            if (review.farmerReply != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = ForestGreenPrimary.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Farmer Reply (${review.farmerName})", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = ForestGreenPrimary)
                        Text("\"${review.farmerReply}\"", style = MaterialTheme.typography.bodySmall, fontSize = 12.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            } else if (isFarmerOwner && onReplySubmit != null) {
                if (!showReplyField) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = { showReplyField = true }) {
                        Icon(Icons.Default.Reply, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reply to Review", fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Write your reply...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (replyText.isNotBlank()) {
                                    onReplySubmit(replyText)
                                    showReplyField = false
                                }
                            },
                            enabled = replyText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreenPrimary)
                        ) {
                            Text("Post")
                        }
                    }
                }
            }
        }
    }
}
