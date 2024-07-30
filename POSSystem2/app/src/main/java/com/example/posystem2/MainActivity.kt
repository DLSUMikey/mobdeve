package com.example.posystem2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
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
                        // Handle order click
                        Toast.makeText(this@MainActivity, "Clicked on order ${order.orderId}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error setting up Order RecyclerView", e)
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
        currentOrderItems.add(item)
        Toast.makeText(this, "${item.itemName} added to order", Toast.LENGTH_SHORT).show()
    }

    private fun finalizeOrder() {
        if (currentOrderItems.isEmpty()) {
            Toast.makeText(this, "No items in the order", Toast.LENGTH_SHORT).show()
            return
        }

        val order = OrderModel(
            orderId = 0,
            orderDate = Date(),
            totalAmount = currentOrderItems.sumOf { it.itemPrice.toDouble() },
            items = currentOrderItems
        )

        lifecycleScope.launch {
            try {
                dbHandler.addOrder(order)
                currentOrderItems.clear()
                Toast.makeText(this@MainActivity, "Order finalized", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error finalizing order", e)
                Toast.makeText(this@MainActivity, "Error finalizing order", Toast.LENGTH_SHORT).show()
            }
        }
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
