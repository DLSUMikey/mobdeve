package com.example.posystem2

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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
    private lateinit var dbHandler: DatabaseHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.title_view)

        dbHandler = DatabaseHandler(this)

        // Add dummy profiles
        GlobalScope.launch {
            try {
                dbHandler.addDummyProfiles()
            } catch (e: Exception) {
                Log.e("MainActivity", "Error adding dummy profiles", e)
            }
        }

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
        val loginBtn2: Button = findViewById(R.id.loginBtn2)
        loginBtn2.setOnClickListener {
            val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val password = findViewById<EditText>(R.id.editTextPass).text.toString()

            GlobalScope.launch {
                try {
                    val profile = dbHandler.getProfileByEmail(email)
                    runOnUiThread {
                        if (profile != null && profile.password == password) {
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
        registerBtn2.setOnClickListener {
            val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val password = findViewById<EditText>(R.id.editTextPass).text.toString()

            GlobalScope.launch {
                try {
                    val existingProfile = dbHandler.getProfileByEmail(email)
                    runOnUiThread {
                        if (existingProfile == null) {
                            val profile = ProfileModel(0, email, password)
                            dbHandler.addProfile(profile)
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
        setContentView(R.layout.activity_main)

        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_700)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { /* Handle Home action */ }
                R.id.nav_profile -> { /* Handle Profile action */ }
                R.id.nav_settings -> { /* Handle Settings action */ }
            }
            drawerLayout.closeDrawers()
            true
        }

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val recyclerView: RecyclerView = findViewById(R.id.mainRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        GlobalScope.launch {
            try {
                val orders = dbHandler.getAllOrders()
                val items = orders.flatMap { it.items }
                runOnUiThread {
                    recyclerView.adapter = MainAdapter(items)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error setting up RecyclerView", e)
            }
        }
    }
}
