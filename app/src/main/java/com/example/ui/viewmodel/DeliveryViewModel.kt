package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.DeliveryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DeliveryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DeliveryRepository
    private var simulationJob: Job? = null

    // Current logged-in user profile
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Active notifications history
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Real-time banner overlay simulation for "Push Updates"
    private val _activeBannerNotification = MutableStateFlow<AppNotification?>(null)
    val activeBannerNotification: StateFlow<AppNotification?> = _activeBannerNotification.asStateFlow()

    // Role switcher helper list for sandbox playground testing
    private val _allDemoUsers = MutableStateFlow<List<User>>(emptyList())
    val allDemoUsers: StateFlow<List<User>> = _allDemoUsers.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = DeliveryRepository(
            userDao = database.userDao(),
            menuItemDao = database.menuItemDao(),
            orderDao = database.orderDao(),
            notificationDao = database.notificationDao()
        )
        
        // Listen to active user notifications reactively
        _currentUser.filterNotNull()
            .flatMapLatest { user ->
                repository.getNotificationsForUser(user.userId)
            }
            .onEach { list ->
                _notifications.value = list
                // If there's a new unread notification, trigger the in-app push notification banner!
                val unread = list.firstOrNull { !it.isRead }
                if (unread != null) {
                    _activeBannerNotification.value = unread
                }
            }
            .launchIn(viewModelScope)

        // Initialize default user
        selectUser(1)
        
        // Start monitoring active picked up orders to continue map simulations
        monitorPendingSimulations()
    }

    // Menu state
    val menuItems = repository.allMenuItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun dismissBanner() {
        _activeBannerNotification.value = null
    }

    fun markAllNotificationsAsRead(userId: Int) {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead(userId)
        }
    }

    fun fetchDemoUsers() {
        viewModelScope.launch {
            val customers = repository.getUsersByRole("CUSTOMER")
            val restaurants = repository.getUsersByRole("RESTAURANT")
            val drivers = repository.getUsersByRole("DRIVER")
            _allDemoUsers.value = customers + restaurants + drivers
        }
    }

    fun selectUser(userId: Int) {
        viewModelScope.launch {
            val user = repository.getUserById(userId)
            _currentUser.value = user
            _activeBannerNotification.value = null
        }
    }

    // Create accounts for Restaurant Owners or Drivers
    fun createAccount(username: String, email: String, role: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val existing = repository.getUserByEmail(email)
            if (existing == null) {
                val newUser = User(
                    username = username,
                    email = email,
                    role = role,
                    balance = if (role == "CUSTOMER") 100.0 else 0.0
                )
                val newId = repository.insertUser(newUser).toInt()
                selectUser(newId)
                fetchDemoUsers()
                onSuccess()
            } else {
                // select user if already exists
                selectUser(existing.userId)
                fetchDemoUsers()
                onSuccess()
            }
        }
    }

    // Shopping Cart state for Customer
    val cart = mutableStateMapOf<MenuItem, Int>()

    fun addToCart(item: MenuItem) {
        cart[item] = (cart[item] ?: 0) + 1
    }

    fun removeFromCart(item: MenuItem) {
        val qty = cart[item] ?: 0
        if (qty > 1) {
            cart[item] = qty - 1
        } else {
            cart.remove(item)
        }
    }

    fun clearCart() {
        cart.clear()
    }

    val cartTotalAmount: Double get() = cart.entries.sumOf { it.key.price * it.value }

    // Payment System Sandbox state
    private val _isProcessingPayment = MutableStateFlow(false)
    val isProcessingPayment: StateFlow<Boolean> = _isProcessingPayment.asStateFlow()

    private val _paymentSuccess = MutableStateFlow<Boolean?>(null)
    val paymentSuccess: StateFlow<Boolean?> = _paymentSuccess.asStateFlow()

    fun resetPaymentState() {
        _paymentSuccess.value = null
    }

    // Complete in-app transaction: subtract fee from customer card details
    fun checkout(address: String, cardNo: String, cardHolder: String, expiry: String, cvc: String, onSuccess: () -> Unit) {
        val customer = _currentUser.value ?: return
        if (customer.role != "CUSTOMER") return

        val total = cartTotalAmount
        if (total <= 0) return

        viewModelScope.launch {
            _isProcessingPayment.value = true
            delay(1800) // Realistic loading feedback delay
            
            if (customer.balance >= total) {
                // Deduct payment securely
                val updatedCustomer = customer.copy(balance = customer.balance - total)
                repository.updateUser(updatedCustomer)
                _currentUser.value = updatedCustomer

                // Generate order details
                val order = Order(
                    customerUserId = customer.userId,
                    customerName = customer.username,
                    restaurantName = "Gourmet Kitchen",
                    status = "PLACED",
                    totalAmount = total,
                    deliveryAddress = address,
                    driverLat = null,
                    driverLng = null,
                    destLat = 51.5200 + (Math.random() - 0.5) * 0.02, // simulated offset in London
                    destLng = -0.1300 + (Math.random() - 0.5) * 0.02
                )

                val itemsList = cart.map { (item, qty) ->
                    OrderItem(
                        orderId = 0,
                        menuItemId = item.id,
                        name = item.name,
                        quantity = qty,
                        price = item.price
                    )
                }

                val orderId = repository.createOrderWithItems(order, itemsList).toInt()
                clearCart()
                _paymentSuccess.value = true
                _isProcessingPayment.value = false

                // Push alerts
                repository.sendNotification(
                    userId = customer.userId,
                    title = "Order Secured!",
                    body = "Payment of $${"%.2f".format(total)} completed successfully. Order Ref #${orderId} is placed."
                )

                // Send notify to Restaurant owner too
                repository.sendNotification(
                    userId = 2, // Prepopulated owner
                    title = "Incoming Order Recieved",
                    body = "New Order #${orderId} for $${"%.2f".format(total)} is waiting for your approval."
                )

                onSuccess()
            } else {
                _paymentSuccess.value = false
                _isProcessingPayment.value = false
                repository.sendNotification(
                    userId = customer.userId,
                    title = "Transaction Declined",
                    body = "Insufficient sandbox wallet funds for $${"%.2f".format(total)}. Please replenish wallet."
                )
            }
        }
    }

    // Add Money to sandbox wallet
    fun addFunds(amount: Double) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(balance = user.balance + amount)
            repository.updateUser(updated)
            _currentUser.value = updated
        }
    }

    // Restaurant menu management actions
    fun addMenuItem(name: String, desc: String, price: Double, category: String) {
        viewModelScope.launch {
            val item = MenuItem(name = name, description = desc, price = price, category = category)
            repository.insertMenuItem(item)
        }
    }

    fun deleteMenuItem(item: MenuItem) {
        viewModelScope.launch {
            repository.deleteMenuItem(item)
        }
    }

    // Reactive streams for listings based on roles
    val allOrders = repository.allOrders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val customerOrders: StateFlow<List<Order>> = _currentUser.filterNotNull()
        .flatMapLatest { user ->
            repository.getOrdersForCustomer(user.userId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val driverOrders: StateFlow<List<Order>> = _currentUser.filterNotNull()
        .flatMapLatest { user ->
            repository.getOrdersForDriver(user.userId)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingOffers = repository.pendingOffers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Order state updates (for Restaurant Owner)
    fun acceptOrderRestaurant(orderId: Int) {
        viewModelScope.launch {
            val order = repository.getOrderById(orderId) ?: return@launch
            val updated = order.copy(status = "PREPARING", etaMinutes = 20)
            repository.updateOrder(updated)

            repository.sendNotification(
                userId = order.customerUserId,
                title = "Order Accepted",
                body = "Your meal is now being meticulously prepared at Gourmet Kitchen! ETA: 20 mins."
            )
        }
    }

    fun makeReadyForPickup(orderId: Int) {
        viewModelScope.launch {
            val order = repository.getOrderById(orderId) ?: return@launch
            val updated = order.copy(status = "READY", etaMinutes = 15)
            repository.updateOrder(updated)

            repository.sendNotification(
                userId = order.customerUserId,
                title = "Order Ready!",
                body = "Your order is freshly prepared and waiting for driver dispatch."
            )

            // Notify all drivers
            val drivers = repository.getUsersByRole("DRIVER")
            drivers.forEach { driver ->
                repository.sendNotification(
                    userId = driver.userId,
                    title = "New Delivery Offer Available",
                    body = "Deliver order #${orderId} to ${order.deliveryAddress}. Est Profit: $10.00."
                )
            }
        }
    }

    // Driver actions
    fun acceptDeliveryDriver(orderId: Int) {
        val driver = _currentUser.value ?: return
        if (driver.role != "DRIVER") return

        viewModelScope.launch {
            val order = repository.getOrderById(orderId) ?: return@launch
            // Driver accepts the delivery request
            val updated = order.copy(
                driverId = driver.userId,
                driverName = driver.username,
                status = "PICKED_UP",
                etaMinutes = 15,
                // Spawn driver coordinates near restaurant (simulated London Center)
                driverLat = order.destLat - 0.012,
                driverLng = order.destLng - 0.012
            )
            repository.updateOrder(updated)

            repository.sendNotification(
                userId = order.customerUserId,
                title = "Driver Assigned",
                body = "Ace Driver ${driver.username} accepted your delivery request and is out to pick up!"
            )
            
            // Trigger GPS animation loops
            startDriverMapSimulation(updated.orderId)
        }
    }

    fun rejectDeliveryDriver(orderId: Int) {
        // Simple mock rejecting - just let user continue or close offer
        viewModelScope.launch {
            val driver = _currentUser.value ?: return@launch
            repository.sendNotification(
                userId = driver.userId,
                title = "Offer Rejected",
                body = "Delivery request #${orderId} dismissed."
            )
        }
    }

    fun deliverOrderDriver(orderId: Int) {
        viewModelScope.launch {
            val order = repository.getOrderById(orderId) ?: return@launch
            val driver = _currentUser.value ?: return@launch
            
            // Update order status to DELIVERED
            val updated = order.copy(
                status = "DELIVERED",
                etaMinutes = 0,
                driverLat = order.destLat,
                driverLng = order.destLng
            )
            repository.updateOrder(updated)

            // Reward driver sandbox balance!
            val updatedDriver = driver.copy(balance = driver.balance + 10.00)
            repository.updateUser(updatedDriver)
            _currentUser.value = updatedDriver

            repository.sendNotification(
                userId = order.customerUserId,
                title = "Order Arrived!",
                body = "Your meal has been delivered securely. Enjoy!"
            )

            repository.sendNotification(
                userId = driver.userId,
                title = "Delivery Complete!",
                body = "$10.00 delivery reward added to your wallet."
            )
        }
    }

    // GIS real-time GPS map simulation tracker
    private fun monitorPendingSimulations() {
        viewModelScope.launch(Dispatchers.IO) {
            // Find any active order that is picked up and simulate movement
            repository.allOrders.collect { orders ->
                val activeSimulations = orders.filter { it.status == "PICKED_UP" && it.driverId != null }
                activeSimulations.forEach { order ->
                    startDriverMapSimulation(order.orderId)
                }
            }
        }
    }

    private var activeSimulations = mutableSetOf<Int>()

    private fun startDriverMapSimulation(orderId: Int) {
        synchronized(activeSimulations) {
            if (activeSimulations.contains(orderId)) return
            activeSimulations.add(orderId)
        }

        viewModelScope.launch(Dispatchers.IO) {
            var step = 0
            while (step < 6) {
                delay(4000) // movement update frequency
                val order = repository.getOrderById(orderId)
                if (order == null || order.status != "PICKED_UP") {
                    break
                }

                val currentLat = order.driverLat ?: (order.destLat - 0.012)
                val currentLng = order.driverLng ?: (order.destLng - 0.012)

                // interpolate 20% closer of the remaining distance to target coordinate
                val nextLat = currentLat + (order.destLat - currentLat) * 0.25
                val nextLng = currentLng + (order.destLng - currentLng) * 0.25
                val nextEta = maxOf(1, order.etaMinutes - 2)

                val updatedOrder = order.copy(
                    driverLat = nextLat,
                    driverLng = nextLng,
                    etaMinutes = nextEta
                )
                repository.updateOrder(updatedOrder)

                // ETA Alert push notifications based on proximity
                if (step == 2) {
                    repository.sendNotification(
                        userId = order.customerUserId,
                        title = "Driver is Nearby!",
                        body = "Your driver is roughly 8 minutes away with your delivery."
                    )
                } else if (step == 4) {
                    repository.sendNotification(
                        userId = order.customerUserId,
                        title = "Almost Arrived",
                        body = "Your driver is pulling up in your street! Look outside."
                    )
                }

                step++
            }

            // Auto-complete simulation if still "PICKED_UP" representing driver arrival
            val finalOrder = repository.getOrderById(orderId)
            if (finalOrder != null && finalOrder.status == "PICKED_UP") {
                val updated = finalOrder.copy(
                    status = "DELIVERED",
                    etaMinutes = 0,
                    driverLat = finalOrder.destLat,
                    driverLng = finalOrder.destLng
                )
                repository.updateOrder(updated)

                // Add driver payout
                finalOrder.driverId?.let { dId ->
                    val driver = repository.getUserById(dId)
                    if (driver != null) {
                        repository.updateUser(driver.copy(balance = driver.balance + 10.00))
                    }
                    repository.sendNotification(
                        userId = dId,
                        title = "Delivery Complete!",
                        body = "You have successfully delivered order #${orderId}. Made $10.00!"
                    )
                }

                repository.sendNotification(
                    userId = finalOrder.customerUserId,
                    title = "Order Delivered!",
                    body = "Our driver has left your meal at your main door or reception. Enjoy!"
                )
            }

            synchronized(activeSimulations) {
                activeSimulations.remove(orderId)
            }
        }
    }
}
