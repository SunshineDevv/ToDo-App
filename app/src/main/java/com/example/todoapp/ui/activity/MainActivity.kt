package com.example.todoapp.ui.activity

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var binding: ActivityMainBinding? = null

    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        binding?.navigationView?.setNavigationItemSelectedListener(this)

        enableEdgeToEdge()

        binding?.main?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
        handleBackPress()

        binding?.navLogout?.setOnClickListener {
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupActionBar() {
        val navController = findNavController()

        val toolbar = AppBarConfiguration(
            topLevelDestinationIds = setOf(R.id.listFragment),
            binding?.drawerLayout
        )

        setSupportActionBar(binding?.toolbar)

        binding?.toolbar?.setupWithNavController(navController, toolbar)

        setupDrawerToggle()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateToolbars(destination)
        }
    }

    private fun updateToolbars(destination: NavDestination) {
        when (destination.id) {
            R.id.noteFragment, R.id.updateNoteFragment -> {
                supportActionBar?.apply {
                    title = "MyNotes"
                    setDisplayHomeAsUpEnabled(true)
                    setHomeButtonEnabled(true)
                }
                drawerToggle.isDrawerIndicatorEnabled = false

                binding?.toolbar?.setNavigationOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }
            }

            else -> {
                supportActionBar?.apply {
                    title = "MyNotes"
                    setDisplayHomeAsUpEnabled(false)
                    setHomeButtonEnabled(false)
                }
                drawerToggle.isDrawerIndicatorEnabled = true
                drawerToggle.syncState()

                binding?.toolbar?.setNavigationOnClickListener {
                    binding?.drawerLayout?.openDrawer(GravityCompat.START)
                }
            }
        }
    }

    private fun findNavController(): NavController {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        return navHostFragment.navController
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val navController = findNavController()
        when (item.itemId) {
            R.id.nav_home -> navController.navigate(R.id.listFragment)
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    private fun handleBackPress() {
        val navController = findNavController()
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding?.drawerLayout?.isDrawerOpen(GravityCompat.START) == true) {
                    binding?.drawerLayout?.closeDrawer(GravityCompat.START)
                } else if (navController.currentDestination?.id == R.id.noteFragment ||
                    navController.currentDestination?.id == R.id.updateNoteFragment
                ) {
                    navController.popBackStack()
                } else {
                    finish()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setupDrawerToggle(){
        drawerToggle = ActionBarDrawerToggle(
            this, binding?.drawerLayout, binding?.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        binding?.drawerLayout?.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}
