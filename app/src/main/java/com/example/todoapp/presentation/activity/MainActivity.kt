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
import android.view.View
import androidx.biometric.BiometricPrompt
import androidx.drawerlayout.widget.DrawerLayout
import com.example.todoapp.domain.biometric.repository.BiometricLockRepository
import com.example.todoapp.presentation.biometric.BiometricPromptManager
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

    @Inject
    lateinit var biometricLockRepository: BiometricLockRepository

    private var binding: ActivityMainBinding? = null

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private var isSessionExpiredHandled = false

    private lateinit var biometricPromptManager: BiometricPromptManager

    private var isBiometricUnlocked = true

    private var biometricPromptVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setupBiometricLock(savedInstanceState)

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

    private fun setupBiometricLock(
        savedInstanceState: Bundle?
    ) {

        biometricPromptManager =
            BiometricPromptManager(this)

        val biometricEnabled =
            biometricLockRepository.isEnabled()

        val requireBiometricOnStart =
            intent.getBooleanExtra(
                EXTRA_REQUIRE_BIOMETRIC_ON_START,
                false
            )

        isBiometricUnlocked =
            if (!biometricEnabled) {

                true

            } else {

                savedInstanceState
                    ?.getBoolean(
                        STATE_BIOMETRIC_UNLOCKED
                    )
                    ?: !requireBiometricOnStart
            }

        binding
            ?.biometricUnlockButton
            ?.setOnClickListener {

                if (!biometricPromptVisible) {
                    showBiometricPrompt()
                }
            }

        binding
            ?.biometricLogoutButton
            ?.setOnClickListener {

                lifecycleScope.launch {

                    logoutUseCase()

                    openAuthActivity()
                }
            }

        renderBiometricLockState()
    }

    private fun showBiometricPrompt() {

        if (!biometricLockRepository.isEnabled()) {
            unlockBiometricUi()
            return
        }

        if (biometricPromptVisible) {
            return
        }

        lockBiometricUi()

        biometricPromptVisible = true

        binding
            ?.biometricLockMessageTextView
            ?.text =
            "Authenticate to access MyNotes."

        biometricPromptManager.authenticate(
            title = "Unlock MyNotes",
            subtitle =
                "Use your biometric to access the app.",
            onSuccess = {

                biometricPromptVisible = false

                unlockBiometricUi()
            },
            onError = { errorCode, errorMessage ->

                biometricPromptVisible = false

                val message =
                    when (errorCode) {

                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT ->
                            "Biometric authentication is temporarily unavailable."

                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_CANCELED ->
                            "MyNotes remains locked."

                        else ->
                            errorMessage.toString()
                    }

                lockBiometricUi(message)
            }
        )
    }

    private fun lockBiometricUi(
        message: String =
            "Biometric authentication is required."
    ) {

        isBiometricUnlocked = false

        binding
            ?.biometricLockMessageTextView
            ?.text =
            message

        binding
            ?.biometricLockOverlay
            ?.visibility =
            View.VISIBLE

        binding
            ?.drawerLayout
            ?.closeDrawers()

        binding
            ?.drawerLayout
            ?.setDrawerLockMode(
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            )
    }

    private fun unlockBiometricUi() {

        isBiometricUnlocked = true

        binding
            ?.biometricLockOverlay
            ?.visibility =
            View.GONE

        binding
            ?.drawerLayout
            ?.setDrawerLockMode(
                DrawerLayout.LOCK_MODE_UNLOCKED
            )
    }

    private fun renderBiometricLockState() {

        if (
            !biometricLockRepository.isEnabled() ||
            isBiometricUnlocked
        ) {
            unlockBiometricUi()
        } else {
            lockBiometricUi()
        }
    }

    override fun onStart() {
        super.onStart()

        if (!biometricLockRepository.isEnabled()) {

            unlockBiometricUi()
            return
        }

        if (
            !isBiometricUnlocked &&
            !biometricPromptVisible
        ) {
            showBiometricPrompt()
        }
    }

    override fun onStop() {

        if (
            biometricLockRepository.isEnabled() &&
            !isChangingConfigurations &&
            !isFinishing &&
            !biometricPromptVisible
        ) {
            lockBiometricUi()
        }

        super.onStop()
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {

        outState.putBoolean(
            STATE_BIOMETRIC_UNLOCKED,
            isBiometricUnlocked
        )

        super.onSaveInstanceState(outState)
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

            R.id.trustedDeviceFragment -> {
                supportActionBar?.apply {
                    title = "Trusted device"

                    setDisplayHomeAsUpEnabled(true)
                    setHomeButtonEnabled(true)

                    setHomeAsUpIndicator(
                        R.drawable.ic_back
                    )
                }
                drawerToggle.isDrawerIndicatorEnabled = false

                binding?.toolbar?.setNavigationOnClickListener {
                        onBackPressedDispatcher
                            .onBackPressed()
                    }
            }

            R.id.biometricLockFragment -> {

                supportActionBar?.apply {
                    title = "Biometric lock"

                    setDisplayHomeAsUpEnabled(true)
                    setHomeButtonEnabled(true)

                    setHomeAsUpIndicator(
                        R.drawable.ic_back
                    )
                }
                drawerToggle.isDrawerIndicatorEnabled = false

                binding?.toolbar?.setNavigationOnClickListener {
                        onBackPressedDispatcher
                            .onBackPressed()
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

                    val name = user.name.orEmpty()
                    val email = user.email

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

    companion object {
        const val EXTRA_REQUIRE_BIOMETRIC_ON_START = "require_biometric_on_start"
        private const val STATE_BIOMETRIC_UNLOCKED = "biometric_unlocked"
    }
}