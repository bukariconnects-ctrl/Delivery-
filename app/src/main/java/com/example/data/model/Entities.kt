package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val username: String,
    val role: String, // "CUSTOMER", "RESTAURANT", "DRIVER"
    val email: String,
    val balance: Double = 100.0 // Default demo wallet balance for payment
)

@Entity(tableName = "menu_items")
data class MenuItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
    val category: String, // "Mains", "Starters", "Drinks", "Desserts"
    val isAvailable: Boolean = true
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val orderId: Int = 0,
    val customerUserId: Int,
    val customerName: String,
    val restaurantId: Int = 1,
    val restaurantName: String,
    val driverId: Int? = null,
    val driverName: String? = null,
    val status: String, // "PLACED", "PREPARING", "READY", "PICKED_UP", "DELIVERED"
    val totalAmount: Double,
    val deliveryAddress: String,
    val createdAt: Long = System.currentTimeMillis(),
    val etaMinutes: Int = 25,
    // GIS simulation variables for Map tracking
    val driverLat: Double? = null,
    val driverLng: Double? = null,
    val destLat: Double = 51.5074, // Default London Center
    val destLng: Double = -0.1278
)

@Entity(tableName = "order_items")
data class OrderItem(
    @PrimaryKey(autoGenerate = true) val orderItemId: Int = 0,
    val orderId: Int,
    val menuItemId: Int,
    val name: String,
    val quantity: Int,
    val price: Double
)

@Entity(tableName = "notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
