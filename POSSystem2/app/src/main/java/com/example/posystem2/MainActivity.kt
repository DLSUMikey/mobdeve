package com.example.posystem2

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ArrayAdapter
import android.widget.Spinner
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
    private var isMainLayoutDisplayed = false

    private val addItemActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            refreshItems()
        }
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        if (isUserLoggedIn()) {
            switchToMainLayout()
        } else {
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
    }

    private fun isUserLoggedIn(): Boolean {
        return sharedPreferences.contains("email")
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
                            val profile = dbHandler.getProfileByEmail(email)
                            saveUserSession(profile!!)
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

    private fun saveUserSession(profile: ProfileModel) {
        val editor = sharedPreferences.edit()
        editor.putInt("userId", profile.id)
        editor.putString("email", profile.email)
        editor.putString("userType", profile.userType)
        editor.apply()
    }

    private fun setupRegistration() {
        setContentView(R.layout.register_view)

        val userTypeSpinner: Spinner = findViewById(R.id.spinnerUserType)
        val userTypes = arrayOf("Employee", "Admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userTypeSpinner.adapter = adapter

        val registerBtn2: Button = findViewById(R.id.registerBtn2)
        val backBtn: Button = findViewById(R.id.backBtn)
        backBtn.setOnClickListener {
            setContentView(R.layout.title_view)
            setupTitleViewButtons()  // Re-bind the title view buttons
        }
        registerBtn2.setOnClickListener {
            val firstName = findViewById<EditText>(R.id.editTextFirstName).text.toString()
            val lastName = findViewById<EditText>(R.id.editTextLastName).text.toString()
            val phoneNumber = findViewById<EditText>(R.id.editTextPhoneNumber).text.toString()
            val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val password = findViewById<EditText>(R.id.editTextPass).text.toString()
            val userType = userTypeSpinner.selectedItem.toString()

            lifecycleScope.launch {
                try {
                    val existingProfile = dbHandler.getProfileByEmail(email)
                    runOnUiThread {
                        if (existingProfile == null) {
                            val profile = ProfileModel(
                                id = 0,
                                email = email,
                                password = password,
                                firstName = firstName,
                                lastName = lastName,
                                phoneNumber = phoneNumber,
                                userType = userType
                            )
                            val newProfileId = dbHandler.addProfile(profile, shouldHashPassword = true) // Hash password for new accounts
                            profile.id = newProfileId.toInt() // Ensure profile ID is updated
                            saveUserSession(profile)
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
        isMainLayoutDisplayed = true
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
        isMainLayoutDisplayed = false
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

    private fun switchToAccountsLayout() {
        isMainLayoutDisplayed = false
        setContentView(R.layout.accounts_view)

        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_700)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupAccountsRecyclerView()
        setupNavigation()
    }

    private fun setupAccountsRecyclerView() {
        val accountsRecyclerView: RecyclerView = findViewById(R.id.accountsRecyclerView)
        accountsRecyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            try {
                val accounts = dbHandler.getAllProfiles()
                runOnUiThread {
                    val adapter = AccountsAdapter(accounts, ::onAccountClick)
                    accountsRecyclerView.adapter = adapter
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching accounts", e)
            }
        }
    }

    private fun setupNavigation() {
        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        val menu = navigationView.menu
        val userType = sharedPreferences.getString("userType", "Employee")

        if (userType == "Employee") {
            menu.findItem(R.id.nav_statistics).isVisible = false
            menu.findItem(R.id.nav_accounts).isVisible = false // Hide accounts for non-admins
        } else if (userType == "Admin") {
            menu.findItem(R.id.nav_statistics).isVisible = true
            menu.findItem(R.id.nav_accounts).isVisible = true // Show accounts for admins
        }

        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    switchToMainLayout()
                }
                R.id.nav_orders -> {
                    switchToOrderView()
                }
                R.id.nav_statistics -> {
                    switchToStatisticsView()
                }
                R.id.nav_accounts -> {
                    switchToAccountsLayout()
                }
                R.id.nav_logout -> {
                    showConfirmationDialog("Are you sure you want to log out?") {
                        logout()
                    }
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun logout() {
        // Clear user session data
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()

        // Navigate to title view
        setContentView(R.layout.title_view)
        setupTitleViewButtons()
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
        val takenByTextView: TextView = dialog.findViewById(R.id.orderTakenByDetailstv) // New TextView for taken by
        val itemsRecyclerView: RecyclerView = dialog.findViewById(R.id.itemsRecyclerView)
        val cancelOrderButton: Button = dialog.findViewById(R.id.cancelOrderButton)
        val completeOrderButton: Button = dialog.findViewById(R.id.completeOrderButton)

        orderIdTextView.text = "Order ID: ${order.orderId}"
        orderDateTextView.text = "Date: ${order.orderDate}"
        totalAmountTextView.text = "Total: ₱${order.totalAmount}"
        orderStatusTextView.text = "Status: ${order.status}"

        lifecycleScope.launch {
            val profile = dbHandler.getProfileById(order.employeeId)
            runOnUiThread {
                if (profile != null) {
                    takenByTextView.text = "Taken by: ${profile.firstName} ${profile.lastName}"
                } else {
                    takenByTextView.text = "Taken by: Unknown"
                }
            }
        }

        itemsRecyclerView.layoutManager = LinearLayoutManager(this)
        itemsRecyclerView.adapter = OrderDetailsAdapter(order.items)

        // Get user type from SharedPreferences
        val userType = sharedPreferences.getString("userType", "Employee")

        // Hide cancel button if the user is not an admin
        if (userType == "Employee") {
            cancelOrderButton.visibility = View.GONE
        }

        cancelOrderButton.setOnClickListener {
            showConfirmationDialog("Are you sure you want to cancel this order?") {
                updateOrderStatus(order, "Cancelled")
                refreshOrders()
                dialog.dismiss()
            }
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

        totalPriceTextView?.text = "Total Price: ₱${String.format("%.2f", totalAmount)}"
        itemCountTextView?.text = "Items: $itemCount"
    }

    private fun finalizeOrder() {
        if (currentOrderItems.isEmpty()) {
            Toast.makeText(this, "No items in the order", Toast.LENGTH_SHORT).show()
            return
        }

        val totalAmount = currentOrderItems.sumOf { it.itemPrice.toDouble() * it.quantity }
        val employeeId = sharedPreferences.getInt("userId", -1) // Assuming userId is stored in shared preferences

        if (employeeId == -1) {
            Toast.makeText(this, "Invalid user. Cannot finalize order.", Toast.LENGTH_SHORT).show()
            return
        }

        val order = OrderModel(
            orderId = 0,
            orderDate = Date(),
            totalAmount = totalAmount,
            items = currentOrderItems.toList(),
            status = "In Progress",
            employeeId = employeeId
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
        recyclerView.adapter = CurrentOrderAdapter(aggregatedItems) { item ->
            removeFromOrder(item)
            dialog.dismiss()  // Dismiss and re-open the dialog to refresh the view
            showCurrentOrderDialog()
        }

        val totalPriceTextView: TextView = dialog.findViewById(R.id.totalPriceTextView)
        val itemCountTextView: TextView = dialog.findViewById(R.id.itemCountTextView)

        val totalAmount = aggregatedItems.sumOf { it.itemPrice.toDouble() * it.quantity }
        val itemCount = aggregatedItems.sumBy { it.quantity }

        totalPriceTextView.text = "Total Price: ₱${String.format("%.2f", totalAmount)}"
        itemCountTextView.text = "Items: $itemCount"

        // Adjust the dialog window size
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val dialogWidth = displayMetrics.widthPixels
        val dialogHeight = (displayMetrics.heightPixels * 0.75).toInt()

        dialog.window?.setLayout(dialogWidth, dialogHeight)

        dialog.show()
    }

    private fun removeFromOrder(item: ItemModel) {
        val existingItem = currentOrderItems.find { it.itemName == item.itemName }
        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                existingItem.quantity -= 1
            } else {
                currentOrderItems.remove(existingItem)
            }
            Toast.makeText(this, "${item.itemName} removed from order", Toast.LENGTH_SHORT).show()
            updateOrderSummary()
        }
    }

    private fun showEditAccountDialog(account: ProfileModel) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_account_edit)

        val accountEmailText: TextView = dialog.findViewById(R.id.accountEmailText)
        val firstNameEditText: EditText = dialog.findViewById(R.id.editTextFirstName)
        val lastNameEditText: EditText = dialog.findViewById(R.id.editTextLastName)
        val phoneNumberEditText: EditText = dialog.findViewById(R.id.editTextPhoneNumber)
        val userTypeSpinner: Spinner = dialog.findViewById(R.id.spinnerUserType)

        accountEmailText.text = account.email
        firstNameEditText.setText(account.firstName)
        lastNameEditText.setText(account.lastName)
        phoneNumberEditText.setText(account.phoneNumber)

        val userTypes = arrayOf("Employee", "Admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        userTypeSpinner.adapter = adapter
        userTypeSpinner.setSelection(userTypes.indexOf(account.userType))

        val saveButton: Button = dialog.findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            val newUserType = userTypeSpinner.selectedItem.toString()
            val updatedAccount = ProfileModel(
                id = account.id,
                email = account.email, // Email remains unchanged
                password = account.password, // Password remains unchanged
                firstName = firstNameEditText.text.toString(),
                lastName = lastNameEditText.text.toString(),
                phoneNumber = phoneNumberEditText.text.toString(),
                userType = newUserType
            )
            lifecycleScope.launch {
                try {
                    dbHandler.updateProfile(updatedAccount)
                    if (account.email == sharedPreferences.getString("email", "")) {
                        updateSharedPreferencesUserType(newUserType)
                    }
                    dialog.dismiss()
                    switchToAccountsLayout() // Refresh the accounts list
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error updating account", e)
                }
            }
        }

        val deleteButton: Button = dialog.findViewById(R.id.deleteButton)
        deleteButton.setOnClickListener {
            showConfirmationDialog("Are you sure you want to delete this account?") {
                lifecycleScope.launch {
                    try {
                        dbHandler.deleteProfile(account.id)
                        dialog.dismiss()
                        switchToAccountsLayout() // Refresh the accounts list
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error deleting account", e)
                    }
                }
            }
        }

        dialog.show()
    }

    private fun updateSharedPreferencesUserType(newUserType: String) {
        val editor = sharedPreferences.edit()
        editor.putString("userType", newUserType)
        editor.apply()
        setupNavigation() // Refresh the navigation menu
    }

    private fun onAccountClick(account: ProfileModel) {
        // Open edit account dialog or activity
        showEditAccountDialog(account)
    }

    private fun onAccountClose(account: ProfileModel) {
        showConfirmationDialog("Are you sure you want to close this account?") {
            dbHandler.deleteProfile(account.id)
            switchToAccountsLayout() // Refresh the accounts list
        }
    }

    private fun showConfirmationDialog(message: String, onConfirm: () -> Unit) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_confirmation)

        val confirmationMessage: TextView = dialog.findViewById(R.id.confirmationMessage)
        val confirmYesButton: Button = dialog.findViewById(R.id.confirmYesButton)
        val confirmNoButton: Button = dialog.findViewById(R.id.confirmNoButton)

        confirmationMessage.text = message

        confirmYesButton.setOnClickListener {
            onConfirm()
            dialog.dismiss()
        }

        confirmNoButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isMainLayoutDisplayed) {
            menuInflater.inflate(R.menu.toolbar_menu, menu)
        }
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

    private fun switchToStatisticsView() {
        isMainLayoutDisplayed = false
        setContentView(R.layout.statistics_view) // Make sure this layout has a RecyclerView and Toolbar like in activity_main

        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_700)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        setupNavigation() // Call setupNavigation to handle navigation items

        val recyclerView: RecyclerView = findViewById(R.id.statisticsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            try {
                val statistics = getAggregatedStatistics()
                runOnUiThread {
                    recyclerView.adapter = StatisticsAdapter(statistics)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error setting up Statistics RecyclerView", e)
            }
        }
    }

    private fun getAggregatedStatistics(): List<StatisticsModel> {
        val itemMap = mutableMapOf<String, Int>()

        // Get all items from the inventory
        val allItems = dbHandler.getAllItems()

        // Initialize all items with count 0
        for (item in allItems) {
            itemMap[item.itemName] = 0
        }

        // Get completed orders and count the items
        val completedOrders = dbHandler.getCompletedOrders()
        for (order in completedOrders) {
            for (item in order.items) {
                val currentCount = itemMap[item.itemName] ?: 0
                itemMap[item.itemName] = currentCount + item.quantity
            }
        }

        return itemMap.map { (itemName, itemCount) -> StatisticsModel(itemName, itemCount) }
            .sortedByDescending { it.itemCount }
    }
}
