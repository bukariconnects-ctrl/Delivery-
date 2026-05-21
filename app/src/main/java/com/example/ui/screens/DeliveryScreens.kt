package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.viewmodel.DeliveryViewModel

@Composable
fun AppPushNotificationBanner(
    banner: AppNotification?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = banner != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp)
            .testTag("push_notification_banner")
    ) {
        if (banner != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notification alert",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = banner.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = banner.body,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss notification",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDeliveryAppContent(
    viewModel: DeliveryViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val activeBanner by viewModel.activeBannerNotification.collectAsState()
    val demoUsers by viewModel.allDemoUsers.collectAsState()

    var activeTab by remember { mutableStateOf("HOME") } // "HOME", "CART", "TRACK", "NOTIF", "ADMIN"
    var showRoleSelectorSheet by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser) {
        viewModel.fetchDemoUsers()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeliveryDining,
                                contentDescription = "App Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Delivery Tracker",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.fetchDemoUsers()
                                showRoleSelectorSheet = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ManageAccounts,
                                contentDescription = "Switch Simulation Accounts"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors()
                )
            },
            bottomBar = {
                if (currentUser != null && currentUser?.role == "CUSTOMER") {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        NavigationBarItem(
                            selected = activeTab == "HOME",
                            onClick = { activeTab = "HOME" },
                            icon = { Icon(Icons.Default.Storefront, "Shop") },
                            label = { Text("Menu") }
                        )
                        NavigationBarItem(
                            selected = activeTab == "CART",
                            onClick = { activeTab = "CART" },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (viewModel.cart.isNotEmpty()) {
                                            Badge {
                                                Text(
                                                    viewModel.cart.values.sum().toString()
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.ShoppingCart, "Cart")
                                }
                            },
                            label = { Text("Cart") }
                        )
                        NavigationBarItem(
                            selected = activeTab == "TRACK",
                            onClick = { activeTab = "TRACK" },
                            icon = { Icon(Icons.Default.Map, "Track map") },
                            label = { Text("Track") }
                        )
                        NavigationBarItem(
                            selected = activeTab == "NOTIF",
                            onClick = { activeTab = "NOTIF" },
                            icon = { Icon(Icons.Default.Notifications, "Alerts") },
                            label = { Text("Alerts") }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (currentUser == null) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    when (currentUser?.role) {
                        "CUSTOMER" -> {
                            when (activeTab) {
                                "HOME" -> CustomerMenuScreen(viewModel)
                                "CART" -> CustomerCartScreen(viewModel, onGoToTracking = { activeTab = "TRACK" })
                                "TRACK" -> CustomerTrackingOrdersScreen(viewModel)
                                "NOTIF" -> CustomerAlertsScreen(viewModel)
                            }
                        }
                        "RESTAURANT" -> {
                            RestaurantOwnerDashboard(viewModel)
                        }
                        "DRIVER" -> {
                            DriverDashboardScreen(viewModel)
                        }
                    }
                }
            }
        }

        // Active Banner Dropdown Alerts for Realtime Status Updates & ETA pushes!
        AppPushNotificationBanner(
            banner = activeBanner,
            onDismiss = { viewModel.dismissBanner() }
        )

        // Account / Role selector modal drawers (Allows easy testing on the web sandbox!)
        if (showRoleSelectorSheet) {
            AlertDialog(
                onDismissRequest = { showRoleSelectorSheet = false },
                title = {
                    Text(
                        text = "Sandbox Environment Switcher",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Switch active roles instantly to simulate interactions (e.g. place order as customer, accept as restaurant, deliver as driver).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Divider()

                        Text(
                            text = "Select Active Profile:",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.labelMedium
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 240.dp)
                        ) {
                            items(demoUsers) { user ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectUser(user.userId)
                                            showRoleSelectorSheet = false
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (currentUser?.userId == user.userId) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when (user.role) {
                                                "CUSTOMER" -> Icons.Default.Person
                                                "RESTAURANT" -> Icons.Default.Storefront
                                                else -> Icons.Default.TwoWheeler
                                            },
                                            contentDescription = null,
                                            tint = if (currentUser?.userId == user.userId) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = user.username,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                            Text(
                                                text = "Role: ${user.role} | Wallet: $${"%.2f".format(user.balance)}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Divider()

                        // Create custom Restaurant Owner or Driver Profile Form
                        var showRegistryForms by remember { mutableStateOf(false) }
                        if (!showRegistryForms) {
                            Button(
                                onClick = { showRegistryForms = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Create Owner or Driver Account")
                            }
                        } else {
                            var regName by remember { mutableStateOf("") }
                            var regEmail by remember { mutableStateOf("") }
                            var regRole by remember { mutableStateOf("RESTAURANT") } // "RESTAURANT" or "DRIVER"
                            
                            OutlinedTextField(
                                value = regName,
                                onValueChange = { regName = it },
                                label = { Text("Account / Business Name") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1
                            )
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = { regEmail = it },
                                label = { Text("Email Contact Address") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Assign Role:", fontWeight = FontWeight.Medium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = regRole == "RESTAURANT",
                                        onClick = { regRole = "RESTAURANT" }
                                    )
                                    Text("Restaurant", fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = regRole == "DRIVER",
                                        onClick = { regRole = "DRIVER" }
                                    )
                                    Text("Driver", fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    if (regName.isNotBlank() && regEmail.isNotBlank()) {
                                        viewModel.createAccount(regName, regEmail, regRole) {
                                            showRoleSelectorSheet = false
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = regName.isNotBlank() && regEmail.isNotBlank()
                            ) {
                                Text("Register & Switch Account")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showRoleSelectorSheet = false }) {
                        Text("Dimiss")
                    }
                }
            )
        }
    }
}

// ----------------------------------------------------
// CUSTOMER - MENU SCREEN
// ----------------------------------------------------
@Composable
fun CustomerMenuScreen(viewModel: DeliveryViewModel) {
    val menuItems by viewModel.menuItems.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val cart = viewModel.cart

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Mains", "Starters", "Drinks", "Desserts")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcoming Card with User wallet
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello, ${currentUser?.username ?: "Customer"}!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Where would you like to deliver today?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp, 6.dp)) {
                        Text("Wallet Balance", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "$${"%.2f".format(currentUser?.balance ?: 0.0)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            modifier = Modifier.testTag("wallet_balance_display")
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Categories list
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Menu Listing
        if (menuItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NoFood,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No dishes on the menu yet.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            val filteredList = if (selectedCategory == "All") menuItems else menuItems.filter { it.category == selectedCategory }
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$${"%.2f".format(item.price)}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.description,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Category: ${item.category}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                
                                val quantityInCart = cart[item] ?: 0
                                if (quantityInCart == 0) {
                                    Button(
                                        onClick = { viewModel.addToCart(item) },
                                        contentPadding = PaddingValues(12.dp, 4.dp),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .testTag("add_to_cart_${item.id}")
                                    ) {
                                        Icon(
                                            Icons.Default.Add, 
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.removeFromCart(item) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.RemoveCircleOutline,
                                                contentDescription = "Remove",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = quantityInCart.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        IconButton(
                                            onClick = { viewModel.addToCart(item) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.AddCircle,
                                                contentDescription = "Add",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// CUSTOMER - CART & SECURE PAYMENTS SCREEN
// ----------------------------------------------------
@Composable
fun CustomerCartScreen(
    viewModel: DeliveryViewModel,
    onGoToTracking: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isProcessingPayment by viewModel.isProcessingPayment.collectAsState()
    val paymentSuccess by viewModel.paymentSuccess.collectAsState()
    val cart = viewModel.cart

    var deliveryAddress by remember { mutableStateOf("12 Baker Street, London, W1U 3BD") }
    var cardNumber by remember { mutableStateOf("4532  8194  2092  1053") }
    var cardHolder by remember { mutableStateOf("John Doe") }
    var expiryDate by remember { mutableStateOf("12/28") }
    var cvvCode by remember { mutableStateOf("542") }

    var showSecureCheckoutModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Your Checkout Basket",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (cart.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBasket,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Your cart is empty.", fontWeight = FontWeight.Bold)
                    Text("Go back to the menu to satisfy your cravings!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Review Items:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(cart.entries.toList()) { (item, quantity) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$${"%.2f".format(item.price)} each",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Qty: ${quantity}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$${"%.2f".format(item.price * quantity)}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Delivery Details",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            OutlinedTextField(
                                value = deliveryAddress,
                                onValueChange = { deliveryAddress = it },
                                label = { Text("Deliver to Address") },
                                modifier = Modifier.fillMaxWidth(),
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // Bottom Subtotal Summary Drawer
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontSize = 13.sp)
                        Text("$${"%.2f".format(viewModel.cartTotalAmount)}", fontSize = 13.sp)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Service fee & Delivery", fontSize = 13.sp)
                        Text("FREE promo", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total Amount", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "$${"%.2f".format(viewModel.cartTotalAmount)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("checkout_total_display")
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { showSecureCheckoutModal = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("checkout_submit_button")
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Secure Payment Integration Checkout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ----------------------------------------------------
        // SECURE IN-APP PAYMENT SYSTEM DIALOG
        // ----------------------------------------------------
        if (showSecureCheckoutModal) {
            AlertDialog(
                onDismissRequest = { if (!isProcessingPayment) showSecureCheckoutModal = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Secure SSL Sandbox Checkout", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isProcessingPayment) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "PROCESSING TRANSACTION SECURELY...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Syncing with bank card gateways...",
                                    fontSize = 10.sp,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            if (paymentSuccess == true) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Success",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Payment Succeeded!",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        "Funds deducted from demo balance securely. Order dispatched!",
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            viewModel.resetPaymentState()
                                            showSecureCheckoutModal = false
                                            onGoToTracking()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Proceed to Live Tracking Map")
                                    }
                                }
                            } else if (paymentSuccess == false) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Declined",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "Transaction Declined",
                                        fontWeight = FontWeight.ExtraBold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        "Insufficient cash. Click below to add digital sandbox funds to balance.",
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            viewModel.addFunds(100.0)
                                            viewModel.resetPaymentState()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Replenish Sandbox +$100.00")
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    TextButton(onClick = { viewModel.resetPaymentState() }) {
                                        Text("Retry Payment Details")
                                    }
                                }
                            } else {
                                // Default payment inputs
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.onSurface,
                                        contentColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "SECURE SANDBOX CREDIT CARD",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                letterSpacing = 1.sp,
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                            )
                                            Icon(
                                                Icons.Default.CreditCard,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(18.dp))
                                        Text(
                                            text = cardNumber.ifEmpty { "••••  ••••  ••••  ••••" },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            letterSpacing = 2.sp
                                        )
                                        Spacer(modifier = Modifier.height(18.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    "CARD HOLDER",
                                                    fontSize = 8.sp,
                                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                                )
                                                Text(
                                                    text = cardHolder.ifEmpty { "YOUR NAME" },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    "EXPIRES",
                                                    fontSize = 8.sp,
                                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                                )
                                                Text(
                                                    text = expiryDate.ifEmpty { "MM/YY" },
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }
                                    }
                                }

                                outField(label = "Card Number", value = cardNumber, onChange = { cardNumber = it }, keyboardType = KeyboardType.Number)
                                outField(label = "Cardholder Name", value = cardHolder, onChange = { cardHolder = it }, keyboardType = KeyboardType.Text)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        outField(label = "Exp Limit", value = expiryDate, onChange = { expiryDate = it }, keyboardType = KeyboardType.Number)
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        outField(label = "CVV/CVC", value = cvvCode, onChange = { cvvCode = it }, keyboardType = KeyboardType.Number)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    if (!isProcessingPayment && paymentSuccess == null) {
                        Button(
                            onClick = {
                                viewModel.checkout(
                                    deliveryAddress,
                                    cardNumber,
                                    cardHolder,
                                    expiryDate,
                                    cvvCode
                                ) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.testTag("pay_and_order_confirm")
                        ) {
                            Text("Pay & Securely Confirm Order")
                        }
                    }
                },
                dismissButton = {
                    if (!isProcessingPayment && paymentSuccess != true) {
                        TextButton(
                            onClick = {
                                viewModel.resetPaymentState()
                                showSecureCheckoutModal = false
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun outField(label: String, value: String, onChange: (String) -> Unit, keyboardType: KeyboardType) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        maxLines = 1
    )
}

// ----------------------------------------------------
// CUSTOMER - TRACKING LIST & REAL-TIME GPS MAP SCREEN
// ----------------------------------------------------
@Composable
fun CustomerTrackingOrdersScreen(viewModel: DeliveryViewModel) {
    val orders by viewModel.customerOrders.collectAsState()
    var selectedOrderForMap by remember { mutableStateOf<Order?>(null) }

    if (selectedOrderForMap != null) {
        // Collect current state of this selected order reactively to show the GPS vehicle marker animating in real-time!
        val currentOrderState = orders.firstOrNull { it.orderId == selectedOrderForMap?.orderId }
        
        if (currentOrderState != null) {
            MapRouteTrackerCanvasSheet(
                order = currentOrderState,
                onClose = { selectedOrderForMap = null }
            )
        } else {
            selectedOrderForMap = null
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Real-Time Order Tracking",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBike,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No Delivery Trips Active",
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Place a checkout order at Gourmet Kitchen and return to track deliveries live!",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(orders) { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOrderForMap = order },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Order #${order.orderId}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        
                                        // Status chips
                                        val statusColor = when (order.status) {
                                            "PLACED" -> Color(0xFF1565C0)
                                            "PREPARING" -> Color(0xFFEF6C00)
                                            "READY" -> Color(0xFFAD1457)
                                            "PICKED_UP" -> Color(0xFF6A1B9A)
                                            else -> Color(0xFF2E7D32)
                                        }
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = statusColor.copy(alpha = 0.15f), contentColor = statusColor),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = order.status,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(6.dp, 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Address: ${order.deliveryAddress}",
                                        fontSize = 11.sp,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Value: $${"%.2f".format(order.totalAmount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    if (order.status == "PICKED_UP") {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.HourglassTop,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "${order.etaMinutes}m ETA",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                    
                                    Button(
                                        onClick = { selectedOrderForMap = order },
                                        contentPadding = PaddingValues(10.dp, 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Open Route Map", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// INTELLIGENT INTERACTIVE VECTOR ROADMAP CANVAS
// ----------------------------------------------------
@Composable
fun MapRouteTrackerCanvasSheet(
    order: Order,
    onClose: () -> Unit
) {
    var displayAlertDetail by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Back Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Live Courier ETA Map",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Order #${order.orderId} matches tracking coordinates",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Live coordinate map drawing
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E2022)) // Pitch dark logistics terminal color
        ) {
            // High fidelity graphics drawing
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw decorative grid backgrounds mimicking digital HUD maps
                val gridSpacing = 60.dp.toPx()
                for (x in 0..(canvasWidth / gridSpacing).toInt()) {
                    drawLine(
                        color = Color(0xFF2C2F33),
                        start = Offset(x * gridSpacing, 0f),
                        end = Offset(x * gridSpacing, canvasHeight),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..(canvasHeight / gridSpacing).toInt()) {
                    drawLine(
                        color = Color(0xFF2C2F33),
                        start = Offset(0f, y * gridSpacing),
                        end = Offset(canvasWidth, y * gridSpacing),
                        strokeWidth = 1f
                    )
                }

                // Draw secondary water canal or green park
                drawCircle(
                    color = Color(0xFF152A2D), // dark emerald park
                    radius = 200f,
                    center = Offset(canvasWidth * 0.7f, canvasHeight * 0.3f)
                )

                // Draw custom highway bypass routes
                drawLine(
                    color = Color(0xFF24272C),
                    start = Offset(0f, canvasHeight * 0.5f),
                    end = Offset(canvasWidth, canvasHeight * 0.5f),
                    strokeWidth = 32f
                )
                drawLine(
                    color = Color(0xFF24272C),
                    start = Offset(canvasWidth * 0.35f, 0f),
                    end = Offset(canvasWidth * 0.35f, canvasHeight),
                    strokeWidth = 32f
                )

                // High fidelity vector paths for major streets
                drawLine(
                    color = Color(0xFF3B3E43),
                    start = Offset(0f, canvasHeight * 0.5f),
                    end = Offset(canvasWidth, canvasHeight * 0.5f),
                    strokeWidth = 4f
                )
                drawLine(
                    color = Color(0xFF3B3E43),
                    start = Offset(canvasWidth * 0.35f, 0f),
                    end = Offset(canvasWidth * 0.35f, canvasHeight),
                    strokeWidth = 4f
                )
                
                // Draw diagonal streets (Route)
                drawLine(
                    color = Color(0xFF3B3E43),
                    start = Offset(200f, canvasHeight * 0.8f),
                    end = Offset(canvasWidth * 0.8f, canvasHeight * 0.2f),
                    strokeWidth = 4f
                )

                // Core coordinates mapping:
                // Map coordinates between fictitious limits:
                // Lat: 51.48 to 51.53, Lng: -0.15 to -0.10
                val minLat = 51.48
                val maxLat = 51.54
                val minLng = -0.15
                val maxLng = -0.09

                fun mapCoordinates(lat: Double, lng: Double): Offset {
                    val x = ((lng - minLng) / (maxLng - minLng)).toFloat() * canvasWidth
                    val y = (1.0f - ((lat - minLat) / (maxLat - minLat)).toFloat()) * canvasHeight
                    return Offset(x, y)
                }

                val restaurantOffset = mapCoordinates(51.492, -0.142) // Constant restaurant hub location
                val destinationOffset = mapCoordinates(order.destLat, order.destLng)

                // Draw routes line
                drawLine(
                    color = Color(0xFFF2A900).copy(alpha = 0.5f),
                    start = restaurantOffset,
                    end = destinationOffset,
                    strokeWidth = 6f,
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(20f, 15f),
                        0f
                    )
                )

                // Restaurant Base Node
                drawCircle(
                    color = Color(0xEED32F2F),
                    radius = 20f,
                    center = restaurantOffset
                )
                drawCircle(
                    color = Color(0xFFD32F2F).copy(alpha = 0.3f),
                    radius = 35f,
                    center = restaurantOffset
                )

                // Customer Destination Base
                drawCircle(
                    color = Color(0xEE1976D2),
                    radius = 20f,
                    center = destinationOffset
                )
                drawCircle(
                    color = Color(0xFF1976D2).copy(alpha = 0.3f),
                    radius = 35f,
                    center = destinationOffset
                )

                // Draw Simulated Driver position!
                if (order.driverLat != null && order.driverLng != null) {
                    val driverPosition = mapCoordinates(order.driverLat, order.driverLng)

                    // Draw proximity sonar ripple
                    drawCircle(
                        color = Color(0xFFF2A900).copy(alpha = 0.3f),
                        radius = 45f,
                        center = driverPosition
                    )
                    drawCircle(
                        color = Color(0xFFF2A900),
                        radius = 16f,
                        center = driverPosition
                    )
                    // Core yellow core
                    drawCircle(
                        color = Color(0xFFFFFFFF),
                        radius = 8f,
                        center = driverPosition
                    )
                }
            }

            // Quick Legend float overlay
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFD32F2F), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gourmet Kitchen", color = Color.White, fontSize = 9.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFF1976D2), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Your House", color = Color.White, fontSize = 9.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(Color(0xFFF2A900), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Active Driver Tracking", color = Color.White, fontSize = 9.sp)
                    }
                }
            }
        }

        // Proximity Dashboard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Trip status timeline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (order.status) {
                                "PLACED" -> "Awaiting Gourmet Acceptance"
                                "PREPARING" -> "Gourmet Kitchen Preparing Meal"
                                "READY" -> "Freshly Cooked! Dispatching Driver"
                                "PICKED_UP" -> "Driver On the Route"
                                else -> "Arrived safely"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Deliver details: ${order.deliveryAddress}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    if (order.status == "PICKED_UP") {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "${order.etaMinutes} MINS ETA",
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp, 4.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Flow timeline visual nodes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TimelineStep(name = "Cooking", isActive = order.status == "PREPARING" || order.status == "READY" || order.status == "PICKED_UP" || order.status == "DELIVERED")
                    TimelineDivider(isActive = order.status == "READY" || order.status == "PICKED_UP" || order.status == "DELIVERED")
                    TimelineStep(name = "Dispatched", isActive = order.status == "READY" || order.status == "PICKED_UP" || order.status == "DELIVERED")
                    TimelineDivider(isActive = order.status == "PICKED_UP" || order.status == "DELIVERED")
                    TimelineStep(name = "On Route", isActive = order.status == "PICKED_UP" || order.status == "DELIVERED")
                    TimelineDivider(isActive = order.status == "DELIVERED")
                    TimelineStep(name = "Arrived", isActive = order.status == "DELIVERED")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Driver contact profiles details
                if (order.driverName != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.DirectionsBike,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = order.driverName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "High Professional Ace Courier • 4.9★",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                            )
                        }
                        IconButton(
                            onClick = { displayAlertDetail = true }
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Call",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Waiting for an available driver around London to pick up your order...",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    if (displayAlertDetail) {
        AlertDialog(
            onDismissRequest = { displayAlertDetail = false },
            title = { Text("Call Driver") },
            text = { Text("Connect sandbox calls to ${order.driverName ?: "Ace Courier"}? (Call will bypass GSM network for demo.)") },
            confirmButton = {
                Button(onClick = { displayAlertDetail = false }) {
                    Text("Connect call")
                }
            },
            dismissButton = {
                TextButton(onClick = { displayAlertDetail = false }) {
                    Text("Skip")
                }
            }
        )
    }
}

@Composable
fun TimelineStep(name: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, fontSize = 9.sp, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun RowScope.TimelineDivider(isActive: Boolean) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(2.dp)
            .background(if (isActive) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f))
    )
}

// ----------------------------------------------------
// CUSTOMER - NOTIFICATION ALERTS SCREEN
// ----------------------------------------------------
@Composable
fun CustomerAlertsScreen(viewModel: DeliveryViewModel) {
    val alerts by viewModel.notifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Notifications Log",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            TextButton(
                onClick = {
                    viewModel.currentUser.value?.userId?.let {
                        viewModel.markAllNotificationsAsRead(it)
                    }
                }
            ) {
                Text("Mark all read", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No updates yet.", fontWeight = FontWeight.Bold)
                    Text("Place checkout orders and enjoy status push highlights.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(alerts) { alert ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (alert.isRead) {
                                MaterialTheme.colorScheme.surface
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                            }
                        ),
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = alert.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = alert.body,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// RESTAURANT OWNER DASHBOARD
// ----------------------------------------------------
@Composable
fun RestaurantOwnerDashboard(viewModel: DeliveryViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()

    var activeSubsection by remember { mutableStateOf("ORDERS") } // "ORDERS", "MENU"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Owner Intro Welcome card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Gourmet Kitchen Controls",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Manage incoming menus dishes, track checkout sales orders pipelines in real-time.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Toggle Buttons sub-pages
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { activeSubsection = "ORDERS" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubsection == "ORDERS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (activeSubsection == "ORDERS") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cuisine Orders")
            }
            Button(
                onClick = { activeSubsection = "MENU" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeSubsection == "MENU") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (activeSubsection == "MENU") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.MenuBook, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Manage Menus")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeSubsection == "ORDERS") {
            // Display Restaurant Orders List
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No customer order logs recorded in sandbox yet.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(orders) { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Order #${order.orderId}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Client: ${order.customerName}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    
                                    // Status
                                    Text(
                                        text = order.status,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                
                                Text(
                                    text = "Destination Address:\n${order.deliveryAddress}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Payout value: $${"%.2f".format(order.totalAmount)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Operations Actions
                                when (order.status) {
                                    "PLACED" -> {
                                        Button(
                                            onClick = { viewModel.acceptOrderRestaurant(order.orderId) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("owner_accept_order_${order.orderId}")
                                        ) {
                                            Text("Approve & Start Preparation Now")
                                        }
                                    }
                                    "PREPARING" -> {
                                        Button(
                                            onClick = { viewModel.makeReadyForPickup(order.orderId) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("owner_cooked_ready_${order.orderId}")
                                        ) {
                                            Text("Mark Meal Cooked & Ready for Courier Dispatch")
                                        }
                                    }
                                    "READY" -> {
                                        Text(
                                            text = "✓ Handing off cooking to Driver Ace Courier",
                                            color = Color(0xFF2E7D32),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    "PICKED_UP" -> {
                                        Text(
                                            text = "🚚 Driver En Route (GPS Live Track Active)",
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    "DELIVERED" -> {
                                        Text(
                                            text = "✓ Order Delivered Safely to Customer Cabinet",
                                            color = Color(0xFF2E7D32),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Manage Menus
            var dishName by remember { mutableStateOf("") }
            var dishDesc by remember { mutableStateOf("") }
            var dishPrice by remember { mutableStateOf("") }
            var dishCat by remember { mutableStateOf("Mains") }

            Column(modifier = Modifier.weight(1f)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Add New Cuisine Dish Form", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        OutlinedTextField(
                            value = dishName,
                            onValueChange = { dishName = it },
                            label = { Text("Dish Name") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1
                        )
                        OutlinedTextField(
                            value = dishDesc,
                            onValueChange = { dishDesc = it },
                            label = { Text("Short Description") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = dishPrice,
                                onValueChange = { dishPrice = it },
                                label = { Text("Price ($)") },
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Box(modifier = Modifier.weight(1f)) {
                                var expandedCatDropdown by remember { mutableStateOf(false) }
                                Button(
                                    onClick = { expandedCatDropdown = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(dishCat, fontSize = 11.sp)
                                    DropdownMenu(
                                        expanded = expandedCatDropdown,
                                        onDismissRequest = { expandedCatDropdown = false }
                                    ) {
                                        val cats = listOf("Mains", "Starters", "Drinks", "Desserts")
                                        cats.forEach { c ->
                                            DropdownMenuItem(
                                                text = { Text(c) },
                                                onClick = {
                                                    dishCat = c
                                                    expandedCatDropdown = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                val pr = dishPrice.toDoubleOrNull() ?: 0.0
                                if (dishName.isNotBlank() && pr > 0.0) {
                                    viewModel.addMenuItem(dishName, dishDesc, pr, dishCat)
                                    dishName = ""
                                    dishDesc = ""
                                    dishPrice = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Insert Dish to Menu Catalog")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Menu Entries:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(menuItems) { d ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = d.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "${d.category} • $${"%.2f".format(d.price)}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(
                                    onClick = { viewModel.deleteMenuItem(d) }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Item", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// DRIVER - COURIER DASHBOARD
// ----------------------------------------------------
@Composable
fun DriverDashboardScreen(viewModel: DeliveryViewModel) {
    val pendingOffers by viewModel.pendingOffers.collectAsState()
    val driverTrips by viewModel.driverOrders.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var activeCourierSection by remember { mutableStateOf("OFFERS") } // "OFFERS", "MY_TRIPS"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Driver metrics header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentUser?.username ?: "Ace Driver",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Courier Duty: Active • Premium Vehicle Rider",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        contentColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Day Earnings", fontSize = 9.sp)
                        Text(
                            text = "$${"%.2f".format(currentUser?.balance ?: 0.0)}",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Subsection navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { activeCourierSection = "OFFERS" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeCourierSection == "OFFERS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (activeCourierSection == "OFFERS") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.LocalMall, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Offer Requests")
            }
            Button(
                onClick = { activeCourierSection = "MY_TRIPS" },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (activeCourierSection == "MY_TRIPS") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (activeCourierSection == "MY_TRIPS") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Active Trips")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeCourierSection == "OFFERS") {
            Text("Open Delivery Offer requests:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            
            // Clean up list
            val filteredOffers = pendingOffers.filter { it.status == "READY" }
            
            if (filteredOffers.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No pending delivery offers at the moment.\nWait for incoming orders as a Customer.",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredOffers) { offer ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Offer Ref #${offer.orderId}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        "PAYOUT: $10.00",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Store: Gourmet Kitchen • Central London Hub",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "To Address: ${offer.deliveryAddress}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.rejectDeliveryDriver(offer.orderId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reject")
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.acceptDeliveryDriver(offer.orderId)
                                            activeCourierSection = "MY_TRIPS"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("driver_accept_delivery_${offer.orderId}")
                                    ) {
                                        Text("Accept Trip")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // MY_TRIPS
            val ongoingTrips = driverTrips.filter { it.status == "PICKED_UP" }
            val completeTrips = driverTrips.filter { it.status == "DELIVERED" }

            Column(modifier = Modifier.weight(1f)) {
                Text("Ongoing Deliveries:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                
                if (ongoingTrips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No active ongoing route delivery assignments.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(ongoingTrips) { trip ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Ongoing Ref: #${trip.orderId}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Customer Location: ${trip.deliveryAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Simulated GPS coordinates tracking active.", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Button(
                                        onClick = { viewModel.deliverOrderDriver(trip.orderId) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("driver_confirm_complete_${trip.orderId}")
                                    ) {
                                        Text("Confirm Package Delivered Successfully")
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Delivery Trip History:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                if (completeTrips.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No final sandbox completed packages logged.", fontSize = 11.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(completeTrips) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Package #${item.orderId}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("Destination: ${item.deliveryAddress}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text("Earned $10.00", color = Color(0xFF2E7D32), fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
