package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class DeliveryRepository(
    private val userDao: UserDao,
    private val menuItemDao: MenuItemDao,
    private val orderDao: OrderDao,
    private val notificationDao: NotificationDao
) {
    // Users
    suspend fun getUserById(userId: Int): User? = userDao.getUserById(userId)
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    suspend fun insertUser(user: User): Long = userDao.insertUser(user)
    suspend fun updateUser(user: User) = userDao.updateUser(user)
    suspend fun getUsersByRole(role: String): List<User> = userDao.getUsersByRole(role)

    // Menu Items
    val allMenuItems: Flow<List<MenuItem>> = menuItemDao.getAllMenuItems()
    suspend fun insertMenuItem(menuItem: MenuItem) = menuItemDao.insertMenuItem(menuItem)
    suspend fun updateMenuItem(menuItem: MenuItem) = menuItemDao.updateMenuItem(menuItem)
    suspend fun deleteMenuItem(menuItem: MenuItem) = menuItemDao.deleteMenuItem(menuItem)

    // Orders
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    fun getOrdersForCustomer(userId: Int): Flow<List<Order>> = orderDao.getOrdersForCustomer(userId)
    fun getOrdersForDriver(driverId: Int): Flow<List<Order>> = orderDao.getOrdersForDriver(driverId)
    val pendingOffers: Flow<List<Order>> = orderDao.getPendingOffers()

    suspend fun getOrderById(orderId: Int): Order? = orderDao.getOrderById(orderId)
    suspend fun insertOrder(order: Order): Long = orderDao.insertOrder(order)
    suspend fun updateOrder(order: Order) = orderDao.updateOrder(order)

    suspend fun getOrderItems(orderId: Int): List<OrderItem> = orderDao.getOrderItems(orderId)

    suspend fun createOrderWithItems(order: Order, items: List<OrderItem>): Long {
        val orderId = orderDao.insertOrder(order).toInt()
        val mappedItems = items.map { it.copy(orderId = orderId) }
        orderDao.insertOrderItems(mappedItems)
        return orderId.toLong()
    }

    // Notifications
    fun getNotificationsForUser(userId: Int): Flow<List<AppNotification>> =
        notificationDao.getNotificationsForUser(userId)

    suspend fun sendNotification(userId: Int, title: String, body: String) {
        notificationDao.insertNotification(
            AppNotification(userId = userId, title = title, body = body)
        )
    }

    suspend fun markAllNotificationsAsRead(userId: Int) =
        notificationDao.markAllAsRead(userId)
}
