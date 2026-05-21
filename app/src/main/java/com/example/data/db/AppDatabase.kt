package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [User::class, MenuItem::class, Order::class, OrderItem::class, AppNotification::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun orderDao(): OrderDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "delivery_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            // Prepopulate some default menus
            val menuItemDao = db.menuItemDao()
            menuItemDao.insertMenuItem(MenuItem(name = "Classic Double Burger", description = "Flame-grilled Wagyu double patty with cheddar cheese, crispy onions, and secret slider sauce.", price = 12.99, category = "Mains"))
            menuItemDao.insertMenuItem(MenuItem(name = "Truffle Mushroom Pizza", description = "Fior di latte, wild forest mushrooms, drizzled with premium black truffle oil and dry oregano.", price = 16.50, category = "Mains"))
            menuItemDao.insertMenuItem(MenuItem(name = "Sweet Potato Fries", description = "Crispy thin-cut sweet potato fries dusted with smoke chipotle sea salt, served with garlic aioli dip.", price = 5.99, category = "Starters"))
            menuItemDao.insertMenuItem(MenuItem(name = "Avocado Quinoa Salad", description = "Freshly diced hass avocados, baby spinach, organic tri-color quinoa, cherry tomatoes, olive citrus drizzle.", price = 10.50, category = "Starters"))
            menuItemDao.insertMenuItem(MenuItem(name = "Wild Berry Lemonade", description = "Fizzy sparkling custom lemon reduction infused with crushed biological blackberries and fresh mint sprigs.", price = 4.25, category = "Drinks"))
            menuItemDao.insertMenuItem(MenuItem(name = "Double Matcha Latte", description = "Authentic stoneground Japanese ceremonial green matcha whisked with warm creamy oat milk and organic honey.", price = 5.50, category = "Drinks"))
            menuItemDao.insertMenuItem(MenuItem(name = "Cheesecake Slice with Caramel", description = "Silky New York style cream cheese slice finished with salted rich caramel sauce and toasted pecans.", price = 7.99, category = "Desserts"))

            // Prepopulate standard demo users
            val userDao = db.userDao()
            userDao.insertUser(User(userId = 1, username = "John Doe (Customer)", role = "CUSTOMER", email = "customer@example.com", balance = 150.00))
            userDao.insertUser(User(userId = 2, username = "Gourmet Kitchen (Owner)", role = "RESTAURANT", email = "owner@example.com"))
            userDao.insertUser(User(userId = 3, username = "Ace Rider (Driver)", role = "DRIVER", email = "driver@example.com"))
        }
    }
}
