package com.example.posystem2

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var dbHandler: DatabaseHandler
    private val currentOrderItems = mutableListOf<ItemModel>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MainAdapter

    private val addItemActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            refreshItems()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.title_view)

        dbHandler = DatabaseHandler(this)

        // Add dummy profiles and items
        lifecycleScope.launch {
            try {
                if (dbHandler.getAllProfiles().isEmpty()) {
                    dbHandler.addDummyProfiles()
                }
                if (dbHandler.getAllItems().isEmpty()) {
                    dbHandler.addDummyItems()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error adding dummy profiles or items", e)
            }
        }

        setupTitleViewButtons()
    }

    private fun setupTitleViewButtons() {
        val loginBtn: Button = findViewById(R.id.loginBtn)
        val registerBtn: Button = findViewById(R.id.registerBtn)

        loginBtn.setOnClickListener {
            setContentView(R.layout.login_view)
            setupLogin()
        }

        registerBtn.setOnClickListener {
            setContentView(R.layout.register_view)
            setupRegistration()
        }
    }

    private fun setupLogin() {
        val backBtn: Button = findViewById(R.id.backBtn)
        val loginBtn2: Button = findViewById(R.id.loginBtn2)
        backBtn.setOnClickListener {
            setContentView(R.layout.title_view)
            setupTitleViewButtons()  // Re-bind the title view buttons
        }
        loginBtn2.setOnClickListener {
            val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val password = findViewById<EditText>(R.id.editTextPass).text.toString()

            lifecycleScope.launch {
                try {
                    val isValid = dbHandler.validateLogin(email, password)
                    runOnUiThread {
                        if (isValid) {
                            switchToMainLayout()
                        } else {
                            Toast.makeText(this@MainActivity, "Invalid email or password", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error during login", e)
                }
            }
        }
    }

    private fun setupRegistration() {
        val registerBtn2: Button = findViewById(R.id.registerBtn2)
        val backBtn: Button = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            setContentView(R.layout.title_view)
            setupTitleViewButtons()  // Re-bind the title view buttons
        }
        registerBtn2.setOnClickListener {
            val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val password = findViewById<EditText>(R.id.editTextPass).text.toString()

            lifecycleScope.launch {
                try {
                    val existingProfile = dbHandler.getProfileByEmail(email)
                    runOnUiThread {
                        if (existingProfile == null) {
                            val profile = ProfileModel(0, email, password)
                            dbHandler.addProfile(profile, shouldHashPassword = true) // Hash password for new accounts
                            switchToMainLayout()
                        } else {
                            Toast.makeText(this@MainActivity, "Email already registered", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error during registration", e)
                }
            }
        }
    }

    private fun switchToMainLayout() {
        setContentView(R.layout.activity_main) // Set the correct layout

        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_700)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupNavigation() // Call setupNavigation to handle navigation items
        setupItemRecyclerView() // Set up items RecyclerView in the main activity layout

        val checkoutBtn: Button = findViewById(R.id.checkoutBtn)
        checkoutBtn.setOnClickListener {
            finalizeOrder()
        }

        val viewCurrentOrderBtn: Button = findViewById(R.id.viewCurrentOrderBtn)
        viewCurrentOrderBtn.setOnClickListener {
            showCurrentOrderDialog()
        }

        updateOrderSummary()
    }

    private fun switchToOrderView() {
        setContentView(R.layout.order_view) // Set the correct layout

        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_700)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupNavigation() // Call setupNavigation to handle navigation items
        setupOrderRecyclerView() // Set up orders RecyclerView in the order view layout
    }

    private fun setupNavigation() {
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchToMainLayout()
                }
                R.id.nav_orders -> {
                    switchToOrderView() // Set up the orders view and toolbar
                }
                R.id.nav_statistics -> { /* Handle Statistics action */ }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupItemRecyclerView() {
        recyclerView = findViewById(R.id.mainRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            try {
                val items = dbHandler.getAllItems().toMutableList()
                runOnUiThread {
                    adapter = MainAdapter(items) { action, item ->
                        handleItemMenuAction(action, item)
                    }
                    recyclerView.adapter = adapter
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error setting up Item RecyclerView", e)
            }
        }
    }

    private fun setupOrderRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.orderRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            try {
                val orders = dbHandler.getAllOrders()
                runOnUiThread {
                    recyclerView.adapter = OrderAdapter(orders) { order ->
                        showOrderDetailsDialog(order)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error setting up Order RecyclerView", e)
            }
        }
    }

    private fun showOrderDetailsDialog(order: OrderModel) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.order_details)

        val orderIdTextView: TextView = dialog.findViewById(R.id.orderIdDetailstv)
        val orderDateTextView: TextView = dialog.findViewById(R.id.orderDateDetailstv)
        val totalAmountTextView: TextView = dialog.findViewById(R.id.orderTotalDetailstv)
        val orderStatusTextView: TextView = dialog.findViewById(R.id.orderStatusDetailstv)
        val itemsRecyclerView: RecyclerView = dialog.findViewById(R.id.itemsRecyclerView)
        val cancelOrderButton: Button = dialog.findViewById(R.id.cancelOrderButton)
        val completeOrderButton: Button = dialog.findViewById(R.id.completeOrderButton)

        orderIdTextView.text = "Order ID: ${order.orderId}"
        orderDateTextView.text = "Date: ${order.orderDate}"
        totalAmountTextView.text = "Total: ₱${order.totalAmount}"
        orderStatusTextView.text = "Status: ${order.status}"

        itemsRecyclerView.layoutManager = LinearLayoutManager(this)
        itemsRecyclerView.adapter = CurrentOrderAdapter(order.items)

        cancelOrderButton.setOnClickListener {
            updateOrderStatus(order, "Cancelled")
            refreshOrders()
            dialog.dismiss()
        }

        completeOrderButton.setOnClickListener {
            updateOrderStatus(order, "Completed")
            refreshOrders()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun updateOrderStatus(order: OrderModel, newStatus: String) {
        val updatedOrder = order.copy(status = newStatus)
        lifecycleScope.launch {
            try {
                dbHandler.updateOrderStatus(updatedOrder)
                Toast.makeText(this@MainActivity, "Order ${updatedOrder.orderId} $newStatus", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error updating order status", e)
                Toast.makeText(this@MainActivity, "Error updating order status", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun refreshItems() {
        lifecycleScope.launch {
            try {
                val items = dbHandler.getAllItems()
                runOnUiThread {
                    adapter.updateItems(items)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error refreshing items", e)
            }
        }
    }

    private fun refreshOrders() {
        lifecycleScope.launch {
            try {
                val orders = dbHandler.getAllOrders()
                runOnUiThread {
                    val recyclerView: RecyclerView = findViewById(R.id.orderRecyclerView)
                    recyclerView.adapter = OrderAdapter(orders) { order ->
                        // Handle order click
                        showOrderDetailsDialog(order)
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error refreshing orders", e)
            }
        }
    }


    private fun handleItemMenuAction(action: MainAdapter.MenuAction, item: ItemModel) {
        when (action) {
            MainAdapter.MenuAction.EDIT -> {
                val intent = Intent(this, AddItemActivity::class.java).apply {
                    putExtra("itemId", item.orderId)
                    putExtra("itemName", item.itemName)
                    putExtra("itemPrice", item.itemPrice)
                    putExtra("imageUri", item.imageUri)
                }
                addItemActivityResultLauncher.launch(intent)
            }
            MainAdapter.MenuAction.ADD -> {
                addToOrder(item)
            }
        }
    }

    private fun addToOrder(item: ItemModel) {
        val existingItem = currentOrderItems.find { it.itemName == item.itemName }
        if (existingItem != null) {
            existingItem.quantity += 1
        } else {
            currentOrderItems.add(item.copy(quantity = 1))
        }
        Toast.makeText(this, "${item.itemName} added to order", Toast.LENGTH_SHORT).show()
        updateOrderSummary()
    }

    private fun updateOrderSummary() {
        val totalAmount = currentOrderItems.sumOf { it.itemPrice.toDouble() * it.quantity }
        val itemCount = currentOrderItems.sumBy { it.quantity }

        val totalPriceTextView: TextView? = findViewById(R.id.totalPriceTextView)
        val itemCountTextView: TextView? = findViewById(R.id.itemCountTextView)

        totalPriceTextView?.text = "Total Price: ₱$totalAmount"
        itemCountTextView?.text = "Items: $itemCount"
    }




    private fun finalizeOrder() {
        if (currentOrderItems.isEmpty()) {
            Toast.makeText(this, "No items in the order", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmount = currentOrderItems.sumOf { it.itemPrice.toDouble() * it.quantity }
        val order = OrderModel(
            orderId = 0,
            orderDate = Date(),
            totalAmount = totalAmount,
            items = currentOrderItems.toList(),
            status = "In Progress" // Default status
        )

        lifecycleScope.launch {
            try {
                dbHandler.addOrder(order)
                currentOrderItems.clear()
                updateOrderSummary() // Reset the order summary
                Toast.makeText(this@MainActivity, "Order finalized", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error finalizing order", e)
                Toast.makeText(this@MainActivity, "Error finalizing order", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun aggregateItems(items: List<ItemModel>): List<ItemModel> {
        val itemMap = mutableMapOf<String, ItemModel>()
        items.forEach { item ->
            if (itemMap.containsKey(item.itemName)) {
                val existingItem = itemMap[item.itemName]!!
                existingItem.quantity += item.quantity
            } else {
                itemMap[item.itemName] = item.copy()
            }
        }
        return itemMap.values.toList()
    }

    private fun showCurrentOrderDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_current_order)

        val aggregatedItems = aggregateItems(currentOrderItems)
        val recyclerView: RecyclerView = dialog.findViewById(R.id.currentOrderRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CurrentOrderAdapter(aggregatedItems)

        val totalPriceTextView: TextView = dialog.findViewById(R.id.totalPriceTextView)
        val itemCountTextView: TextView = dialog.findViewById(R.id.itemCountTextView)

        val totalAmount = aggregatedItems.sumOf { it.itemPrice.toDouble() * it.quantity }
        val itemCount = aggregatedItems.sumBy { it.quantity }

        totalPriceTextView.text = "Total Price: ₱$totalAmount"
        itemCountTextView.text = "Items: $itemCount"

        dialog.show()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_item -> {
                val intent = Intent(this, AddItemActivity::class.java)
                addItemActivityResultLauncher.launch(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
