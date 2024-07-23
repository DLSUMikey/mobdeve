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
import com.example.possystem2.MainAdapter

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

        // Set up the RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.mainRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Create dummy data
        val items = listOf(
            ItemModel(R.drawable.ic_launcher_background, "Item 1", 10),
            ItemModel(R.drawable.ic_launcher_background, "Item 2", 20),
            ItemModel(R.drawable.ic_launcher_background, "Item 3", 30),
            ItemModel(R.drawable.ic_launcher_background, "Item 4", 40)
        )

        // Set the adapter with the dummy data
        val adapter = MainAdapter(items)
        recyclerView.adapter = adapter
    }
}
