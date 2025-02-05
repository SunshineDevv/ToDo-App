package com.example.todoapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
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
import com.example.todoapp.databinding.ItemHeaderNavBinding
import com.example.todoapp.ui.fragment.security.SecurePreferencesHelper
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener , ToolbarManager{

    private var binding: ActivityMainBinding? = null

    private val firebaseAuth = FirebaseAuth.getInstance()

    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        setupHeaderOfDrawer()

        setupLogOut()

        binding?.navigationView?.setNavigationItemSelectedListener(this)

        enableEdgeToEdge()

        binding?.main?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
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
            R.id.noteFragment, R.id.updateNoteFragment-> {
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

            R.id.securityFragment -> {
                supportActionBar?.apply {
                    title = "Security configuration"
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
            R.id.nav_settings -> navController.navigate(R.id.settingsFragment)
        }
        binding?.drawerLayout?.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setupDrawerToggle(){
        drawerToggle = ActionBarDrawerToggle(
            this, binding?.drawerLayout, binding?.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        binding?.drawerLayout?.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun setupHeaderOfDrawer(){
        val headerView = binding?.navigationView?.getHeaderView(0)

        val headerBinding = headerView?.let { ItemHeaderNavBinding.bind(it) }

        headerBinding?.userNameTextView?.text = firebaseAuth.currentUser?.displayName
        headerBinding?.userEmailTextView?.text = firebaseAuth.currentUser?.email
    }

    private fun setupLogOut(){
        binding?.navLogout?.setOnClickListener{
            SecurePreferencesHelper.saveSuccess(this, "false")
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    override fun showBackButton(show: Boolean) {
        supportActionBar?.setDisplayHomeAsUpEnabled(show)
        supportActionBar?.setHomeButtonEnabled(show)
        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

    }

    override fun enableDrawer(enabled: Boolean) {
        drawerToggle.isDrawerIndicatorEnabled = enabled
        if(enabled){
            drawerToggle.syncState()
            binding?.toolbar?.setNavigationOnClickListener {
                binding?.drawerLayout?.openDrawer(GravityCompat.START)
            }
        }
    }

    override fun setNavigationIcon(isBackArrow: Boolean) {
        if (isBackArrow) {
            supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back)
        } else {
            drawerToggle.syncState()
        }
    }
}

interface ToolbarManager {
    fun showBackButton(show: Boolean)
    fun enableDrawer(enabled: Boolean)
    fun setNavigationIcon(isBackArrow: Boolean)
}
