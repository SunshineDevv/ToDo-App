package com.example.todoapp.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.databinding.ItemHeaderNavBinding
import com.example.todoapp.domain.auth.usecase.GetCurrentUserUseCase
import com.example.todoapp.domain.auth.usecase.LogoutUseCase
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.todoapp.core.session.AuthSessionState
import com.example.todoapp.core.session.AuthSessionEventManager
import com.example.todoapp.core.result.AppResult

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    ToolbarManager {

    @Inject
    lateinit var logoutUseCase: LogoutUseCase

    @Inject
    lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @Inject
    lateinit var authSessionEventManager: AuthSessionEventManager

    private var binding: ActivityMainBinding? = null

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private var isSessionExpiredHandled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupActionBar()

        authSessionEventManager.markAuthenticated()

        observeAuthSessionState()

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

    private fun setupDrawerToggle() {
        drawerToggle = ActionBarDrawerToggle(
            this, binding?.drawerLayout, binding?.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        binding?.drawerLayout?.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun setupHeaderOfDrawer() {
        val headerView = binding?.navigationView?.getHeaderView(0) ?: return
        val headerBinding = ItemHeaderNavBinding.bind(headerView)

        headerBinding.userNameTextView.text = "Loading..."
        headerBinding.userEmailTextView.text = ""

        lifecycleScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val user = result.data

                    val name = user?.name.orEmpty()
                    val email = user?.email.orEmpty()

                    when {
                        name.isNotBlank() && email.isNotBlank() -> {
                            headerBinding.userNameTextView.text = name
                            headerBinding.userEmailTextView.text = email
                        }

                        name.isNotBlank() -> {
                            headerBinding.userNameTextView.text = name
                            headerBinding.userEmailTextView.text = ""
                        }

                        email.isNotBlank() -> {
                            headerBinding.userNameTextView.text = email
                            headerBinding.userEmailTextView.text = ""
                        }

                        else -> {
                            headerBinding.userNameTextView.text = "User"
                            headerBinding.userEmailTextView.text = ""
                        }
                    }

                    Log.i("BACKEND_PROFILE", "current user loaded")
                }

                is AppResult.Failure -> {
                    Log.e("BACKEND_PROFILE", "failed to load current user: ${result.error}")

                    headerBinding.userNameTextView.text = "User"
                    headerBinding.userEmailTextView.text = ""
                }
            }
        }
    }

    private fun setupLogOut() {
        binding?.navLogout?.setOnClickListener {
            lifecycleScope.launch {
                logoutUseCase()
                Log.i("BACKEND_LOGOUT", "user logged out from app")

                openAuthActivity()
            }
        }
    }

    private fun observeAuthSessionState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authSessionEventManager.sessionState.collect { state ->
                    when (state) {
                        AuthSessionState.Authenticated -> {}

                        AuthSessionState.Unauthenticated -> {
                            handleSessionExpired()
                        }
                    }
                }
            }
        }
    }

    private fun handleSessionExpired() {
        if (isSessionExpiredHandled) {
            return
        }

        isSessionExpiredHandled = true

        Log.i("BACKEND_SESSION", "unauthenticated session state received")
        Toast.makeText(
            this,
            "Session expired. Please log in again.",
            Toast.LENGTH_SHORT
        ).show()

        openAuthActivity()
    }

    private fun openAuthActivity() {
        val intent = Intent(this@MainActivity, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
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
        if (enabled) {
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