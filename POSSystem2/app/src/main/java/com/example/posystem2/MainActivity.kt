package com.example.posystem2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set the status bar color
        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_700)

        // Set up the toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up the navigation drawer
        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Handle Home action
                }
                R.id.nav_profile -> {
                    // Handle Profile action
                }
                R.id.nav_settings -> {
                    // Handle Settings action
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        // Initialize the database
        val databaseHandler = DatabaseHandler(this)

        // Create dummy data and insert it into the database
        GlobalScope.launch {
            val items = listOf(
                ItemModel(imageId = R.drawable.ic_launcher_background, itemName = "Item 1", itemPrice = 10),
                ItemModel(imageId = R.drawable.ic_launcher_background, itemName = "Item 2", itemPrice = 20),
                ItemModel(imageId = R.drawable.ic_launcher_background, itemName = "Item 3", itemPrice = 30)
            )
            val order1 = OrderModel(orderId = 0, orderDate = Date(), totalAmount = 60.0, items = items)
            databaseHandler.addOrder(order1)

            val orders = databaseHandler.getAllOrders()

            // Log the retrieved data for debugging purposes
            orders.forEach { order ->
                println("Order ID: ${order.orderId}, Date: ${order.orderDate}, Total Amount: ${order.totalAmount}")
                order.items.forEach { item ->
                    println("  Item Name: ${item.itemName}, Price: ${item.itemPrice}")
                }
            }
        }
    }
}
