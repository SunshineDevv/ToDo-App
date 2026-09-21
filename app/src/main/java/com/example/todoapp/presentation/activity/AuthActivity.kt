package com.example.todoapp.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.todoapp.R
import com.example.todoapp.databinding.ActivityAuthBinding
import com.example.todoapp.domain.auth.model.RestoreSessionResult
import com.example.todoapp.domain.auth.usecase.CheckAuthStateUseCase
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity(), ActivityUIController {

    private var binding: ActivityAuthBinding? = null

    @Inject
    lateinit var checkAuthStateUseCase: CheckAuthStateUseCase

    private lateinit var navController: NavController

    private var isCheckingAuth = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            isCheckingAuth
        }

        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)

        enableEdgeToEdge()
        setContentView(binding?.root)

        setupWindowInsets()
        setupNavigation()
        setupActionBar()
        setupTabLayout()
        setupVisualBehavior()

        if (isPasswordResetDeepLink(intent)) {
            isCheckingAuth = false
            navController.handleDeepLink(intent)
        } else {
            checkInitialAuthState()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        if (::navController.isInitialized && isPasswordResetDeepLink(intent)) {
            isCheckingAuth = false
            navController.handleDeepLink(intent)
        }
    }

    private fun setupWindowInsets() {
        binding?.mainAuth?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { view, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                view.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
                )

                insets
            }
        }
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment

        navController = navHostFragment.navController
    }

    private fun checkInitialAuthState() {
        lifecycleScope.launch {
            try {
                when (checkAuthStateUseCase()) {
                    RestoreSessionResult.Authenticated -> {
                        openMainActivity()
                    }

                    RestoreSessionResult.Unauthenticated -> {
                        isCheckingAuth = false
                    }

                    RestoreSessionResult.NetworkUnavailable -> {
                        isCheckingAuth = false

                        Toast.makeText(
                            this@AuthActivity,
                            "No internet connection. Please try again later.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    RestoreSessionResult.ServerUnavailable -> {
                        isCheckingAuth = false

                        Toast.makeText(
                            this@AuthActivity,
                            "Authentication server is temporarily unavailable.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                isCheckingAuth = false

                Toast.makeText(
                    this@AuthActivity,
                    "Authentication check failed.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun openMainActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        startActivity(intent)
        finish()
    }

    private fun setupTabLayout() {
        binding?.tabLayout?.apply {
            addTab(newTab().setText("Log in"))
            addTab(newTab().setText("Sign up"))
        }

        binding?.tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        if (navController.currentDestination?.id != R.id.logInFragment) {
                            navController.navigate(R.id.logInFragment)
                        }
                    }

                    1 -> {
                        if (navController.currentDestination?.id != R.id.signUpFragment) {
                            navController.navigate(R.id.signUpFragment)
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit

            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.logInFragment -> {
                    binding?.tabLayout?.selectTab(binding?.tabLayout?.getTabAt(0))
                }

                R.id.signUpFragment -> {
                    binding?.tabLayout?.selectTab(binding?.tabLayout?.getTabAt(1))
                }
            }
        }
    }

    private fun setupVisualBehavior() {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.twoAuthFragment, R.id.forgetPassFragment, R.id.resetPassFragment -> {
                    binding?.toolbar?.visibility = View.VISIBLE
                    toggleFullScreenMode(true)
                }

                else -> {
                    toggleFullScreenMode(false)
                }
            }
        }
    }

    private fun toggleFullScreenMode(isFullScreen: Boolean) {
        binding?.apply {
            if (isFullScreen) {
                val fragmentParams = fragmentContainerView.layoutParams as ConstraintLayout.LayoutParams
                fragmentParams.topToBottom = R.id.toolbar
                fragmentParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                fragmentContainerView.layoutParams = fragmentParams

                tabLayout.visibility = View.GONE
                imageView.visibility = View.GONE
                textViewMyNotes.visibility = View.GONE
            } else {
                val fragmentParams = fragmentContainerView.layoutParams as ConstraintLayout.LayoutParams
                fragmentParams.topToBottom = R.id.tabLayout
                fragmentParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                fragmentContainerView.layoutParams = fragmentParams

                tabLayout.visibility = View.VISIBLE
                textViewMyNotes.visibility = View.VISIBLE
                imageView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupActionBar() {
        val toolbar = AppBarConfiguration(
            topLevelDestinationIds = setOf(R.id.logInFragment)
        )

        setSupportActionBar(binding?.toolbar)

        binding?.toolbar?.setupWithNavController(navController, toolbar)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateToolbar(destination)
        }
    }

    private fun updateToolbar(destination: NavDestination) {
        when (destination.id) {
            R.id.twoAuthFragment -> {
                supportActionBar?.apply {
                    title = "2FA Authentication"
                    setDisplayHomeAsUpEnabled(true)
                    setHomeButtonEnabled(true)
                }

                binding?.toolbar?.setNavigationOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }
            }

            R.id.forgetPassFragment -> {
                supportActionBar?.apply {
                    title = "Recovering password"
                    setDisplayHomeAsUpEnabled(true)
                    setHomeButtonEnabled(true)
                }

                binding?.toolbar?.setNavigationOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }
            }

            R.id.resetPassFragment -> {
                supportActionBar?.apply {
                    title = "Reset password"
                    setDisplayHomeAsUpEnabled(true)
                    setHomeButtonEnabled(true)
                }

                binding?.toolbar?.setNavigationOnClickListener {
                    onBackPressedDispatcher.onBackPressed()
                }
            }
            else -> {
                supportActionBar?.apply {
                    setDisplayHomeAsUpEnabled(false)
                    setHomeButtonEnabled(false)
                }
                binding?.toolbar?.visibility = View.INVISIBLE
            }
        }
    }

    private fun isPasswordResetDeepLink(intent: Intent?): Boolean {
        val data = intent?.data ?: return false

        return data.scheme == PASSWORD_RESET_SCHEME &&
                data.host == PASSWORD_RESET_HOST &&
                !data.getQueryParameter(ARG_TOKEN).isNullOrBlank()
    }

    override fun showProgressBar(show: Boolean) {
        if (show){
            binding?.progressIndicator?.visibility = View.VISIBLE
            binding?.dimOverlay?.visibility = View.VISIBLE
        } else {
            binding?.progressIndicator?.visibility = View.GONE
            binding?.dimOverlay?.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    private companion object {
        const val PASSWORD_RESET_SCHEME = "todoapp"
        const val PASSWORD_RESET_HOST = "reset-password"
        const val ARG_TOKEN = "token"
    }
}