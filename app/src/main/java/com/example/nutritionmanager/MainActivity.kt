package com.example.nutritionmanager

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.example.nutritionmanager.ui.*
import com.example.nutritionmanager.utils.SessionManager

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var currentUserId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sessionManager = SessionManager(this)

        // Kiểm tra đăng nhập
        if (!sessionManager.isLoggedIn()) {
            goToLoginActivity()
            return
        }

        currentUserId = sessionManager.getUserId()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "NutriTrack Pro"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        val fullName = sessionManager.getFullName()
        val username = sessionManager.getUsername()
        supportActionBar?.subtitle = if (fullName.isNotEmpty()) "Xin chào, $fullName" else "Xin chào, $username"

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_dashboard -> {
                loadFragment(DashboardFragment())
                true
            }
            R.id.action_add_meal -> {
                loadFragment(AddMealFragment())
                true
            }
            R.id.action_history -> {
                loadFragment(HistoryFragment())
                true
            }
            R.id.action_chart -> {
                loadFragment(ChartFragment())
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun logout() {
        sessionManager.logout()
        goToLoginActivity()
    }

    private fun goToLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    companion object {
        var currentUserId: Int = -1
            private set

        fun setCurrentUserId(userId: Int) {
            currentUserId = userId
        }
    }
}